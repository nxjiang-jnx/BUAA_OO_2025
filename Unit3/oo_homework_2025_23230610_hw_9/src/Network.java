import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.exceptions.TagIdNotFoundException;
import com.oocourse.spec1.exceptions.RelationNotFoundException;
import com.oocourse.spec1.exceptions.EqualTagIdException;
import com.oocourse.spec1.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec1.main.NetworkInterface;
import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface;

import java.util.HashMap;
import java.util.HashSet;

public class Network implements NetworkInterface {
    private final HashMap<Integer, Person> persons;
    private final HashMap<Integer, Integer> rootMap;
    private final HashMap<Integer, Integer> depthMap;
    private int tripleSum;
    private int blockSum;

    public Network() {
        persons = new HashMap<>();
        rootMap = new HashMap<>();
        depthMap = new HashMap<>();
        tripleSum = 0;
        blockSum = 0;
    }

    @Override
    public boolean containsPerson(int id) {
        return persons.containsKey(id);
    }

    @Override
    public PersonInterface getPerson(int id) {
        return persons.getOrDefault(id, null);
    }

    @Override
    public void addPerson(PersonInterface person) throws EqualPersonIdException {
        if (persons.containsKey(person.getId())) {
            throw new EqualPersonIdException(person.getId());
        }
        persons.put(person.getId(), (Person) person);
        rootMap.put(person.getId(), person.getId());
        depthMap.put(person.getId(), 1);    // 并查集树深度为1
        blockSum++;
    }

    @Override
    public void addRelation(int id1, int id2, int value) throws
        PersonIdNotFoundException, EqualRelationException {
        if (!persons.containsKey(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        if (!persons.containsKey(id2)) {
            throw new PersonIdNotFoundException(id2);
        }
        if (persons.get(id1).isLinked(persons.get(id2))) {
            throw new EqualRelationException(id1, id2);
        }
        persons.get(id1).addLink(persons.get(id2), value);
        persons.get(id2).addLink(persons.get(id1), value);
        tripleSum += persons.get(id1).getAcquaintanceCount() <
                persons.get(id2).getAcquaintanceCount() ?
                persons.get(id1).getTripleSum(persons.get(id2)) :
                persons.get(id2).getTripleSum(persons.get(id1));
        mergeBlocks(id1, id2);
    }

    @Override
    public void modifyRelation(int id1, int id2, int value) throws
        PersonIdNotFoundException, EqualPersonIdException, RelationNotFoundException {
        if (!persons.containsKey(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        if (!persons.containsKey(id2)) {
            throw new PersonIdNotFoundException(id2);
        }
        if (id1 == id2) {
            throw new EqualPersonIdException(id1);
        }
        if (!persons.get(id1).isLinked(persons.get(id2))) {
            throw new RelationNotFoundException(id1, id2);
        }

        if (queryValue(id1, id2) + value <= 0) {
            // 删除三元环
            tripleSum -= persons.get(id1).getAcquaintanceCount() <
                    persons.get(id2).getAcquaintanceCount() ?
                    persons.get(id1).getTripleSum(persons.get(id2)) :
                    persons.get(id2).getTripleSum(persons.get(id1));

            persons.get(id1).delLink(persons.get(id2), value);
            persons.get(id2).delLink(persons.get(id1), value);

            // 获取 id1 所在连通图所有节点
            HashSet<Integer> id1Set = new HashSet<>();
            dfs(id1, id1Set);

            if (!id1Set.contains(id2)) {
                // 更新 id2 并查集
                HashSet<Integer> id2Set = new HashSet<>();
                dfs(id2, id2Set);
                for (Integer id : id2Set) {
                    rootMap.put(id, id2);
                    depthMap.put(id, 1);
                }
                // 图已分裂成两个块
                blockSum++;
                depthMap.put(id2, 2);
            }

            // 更新 id1 并查集
            for (Integer id : id1Set) {
                rootMap.put(id, id1);
                depthMap.put(id, 1);
            }
            depthMap.put(id1, 2);
        } else {
            // 调整关系
            persons.get(id1).modifyLink(persons.get(id2), value);
            persons.get(id2).modifyLink(persons.get(id1), value);
        }
    }

    @Override
    public int queryValue(int id1, int id2) throws
        PersonIdNotFoundException, RelationNotFoundException {
        if (!persons.containsKey(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        if (!persons.containsKey(id2)) {
            throw new PersonIdNotFoundException(id2);
        }
        if (!persons.get(id1).isLinked(persons.get(id2))) {
            throw new RelationNotFoundException(id1, id2);
        }
        return persons.get(id1).queryValue(persons.get(id2));
    }

    @Override
    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        // 判断连通性
        if (!persons.containsKey(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        if (!persons.containsKey(id2)) {
            throw new PersonIdNotFoundException(id2);
        }
        return findRoot(id1) == findRoot(id2);
    }

    @Override
    public int queryTripleSum() {
        return tripleSum;
    }

    @Override
    public void addTag(int personId, TagInterface tag) throws
        PersonIdNotFoundException, EqualTagIdException {
        if (!persons.containsKey(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (persons.get(personId).containsTag(tag.getId())) {
            throw new EqualTagIdException(tag.getId());
        }
        persons.get(personId).addTag(tag);
    }

    @Override
    public void addPersonToTag(int personId1, int personId2, int tagId) throws
        PersonIdNotFoundException, RelationNotFoundException,
        TagIdNotFoundException, EqualPersonIdException {
        if (!persons.containsKey(personId1)) {
            throw new PersonIdNotFoundException(personId1);
        }
        if (!persons.containsKey(personId2)) {
            throw new PersonIdNotFoundException(personId2);
        }
        if (personId1 == personId2) {
            throw new EqualPersonIdException(personId1);
        }
        if (!persons.get(personId1).isLinked(persons.get(personId2))) {
            throw new RelationNotFoundException(personId1, personId2);
        }
        if (!persons.get(personId2).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }
        if (persons.get(personId2).getTag(tagId).hasPerson(persons.get(personId1))) {
            throw new EqualPersonIdException(personId1);
        }

        if (persons.get(personId2).getTag(tagId).getSize() <= 999) {
            persons.get(personId2).getTag(tagId).addPerson(persons.get(personId1));
        }
    }

    @Override
    public int queryTagAgeVar(int personId, int tagId) throws
        PersonIdNotFoundException, TagIdNotFoundException {
        if (!persons.containsKey(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (!persons.get(personId).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }
        return persons.get(personId).getTag(tagId).getAgeVar();
    }

    @Override
    public void delPersonFromTag(int personId1, int personId2, int tagId) throws
        PersonIdNotFoundException, TagIdNotFoundException {
        if (!persons.containsKey(personId1)) {
            throw new PersonIdNotFoundException(personId1);
        }
        if (!persons.containsKey(personId2)) {
            throw new PersonIdNotFoundException(personId2);
        }
        if (!persons.get(personId2).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }
        if (!persons.get(personId2).getTag(tagId).hasPerson(persons.get(personId1))) {
            throw new PersonIdNotFoundException(personId1);
        }
        persons.get(personId2).getTag(tagId).delPerson(persons.get(personId1));
    }

    @Override
    public void delTag(int personId, int tagId) throws
        PersonIdNotFoundException, TagIdNotFoundException {
        if (!persons.containsKey(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (!persons.get(personId).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }
        persons.get(personId).delTag(tagId);
    }

    @Override
    public int queryBestAcquaintance(int id) throws
        PersonIdNotFoundException, AcquaintanceNotFoundException {
        if (!persons.containsKey(id)) {
            throw new PersonIdNotFoundException(id);
        }
        if (persons.get(id).getAcquaintance().isEmpty()) {
            throw new AcquaintanceNotFoundException(id);
        }
        return persons.get(id).getBestAcquaintance();
    }

    private int findRoot(int id) {
        // 查找并查集树根节点，采用路径压缩
        if (rootMap.get(id) != id) {
            rootMap.put(id, findRoot(rootMap.get(id)));
        }
        return rootMap.get(id);
    }

    private void mergeBlocks(int id1, int id2) {
        int root1 = findRoot(id1);
        int root2 = findRoot(id2);
        if (root1 == root2) {
            return;
        }
        blockSum--;
        if (depthMap.get(root1) > depthMap.get(root2)) {
            rootMap.put(root2, root1);
        } else {
            rootMap.put(root1, root2);
            if (depthMap.get(root1).equals(depthMap.get(root2))) {
                depthMap.put(root2, depthMap.get(root2) + 1);
            }
        }
    }

    private void dfs(int id, HashSet<Integer> visited) {
        // 深度优先搜索，用于找到所有顶点
        visited.add(id);
        for (Integer personId : persons.get(id).getAcquaintance().keySet()) {
            if (!visited.contains(personId)) {
                dfs(personId, visited);
            }
        }
    }

    public PersonInterface[] getPersons() {
        return persons.values().toArray(new PersonInterface[0]);
    }
}