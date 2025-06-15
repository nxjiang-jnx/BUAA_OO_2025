import com.oocourse.spec2.exceptions.EqualPersonIdException;
import com.oocourse.spec2.exceptions.EqualRelationException;
import com.oocourse.spec2.exceptions.PersonIdNotFoundException;
import com.oocourse.spec2.exceptions.RelationNotFoundException;
import com.oocourse.spec2.exceptions.EqualTagIdException;
import com.oocourse.spec2.exceptions.TagIdNotFoundException;
import com.oocourse.spec2.exceptions.EqualArticleIdException;
import com.oocourse.spec2.exceptions.ArticleIdNotFoundException;
import com.oocourse.spec2.exceptions.EqualOfficialAccountIdException;
import com.oocourse.spec2.exceptions.OfficialAccountIdNotFoundException;
import com.oocourse.spec2.exceptions.PathNotFoundException;
import com.oocourse.spec2.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec2.exceptions.DeleteOfficialAccountPermissionDeniedException;
import com.oocourse.spec2.exceptions.ContributePermissionDeniedException;
import com.oocourse.spec2.exceptions.DeleteArticlePermissionDeniedException;
import com.oocourse.spec2.main.NetworkInterface;
import com.oocourse.spec2.main.PersonInterface;
import com.oocourse.spec2.main.TagInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Network implements NetworkInterface {
    private final HashMap<Integer, Person> persons;
    private final HashMap<Integer, Integer> rootMap;
    private final HashMap<Integer, Integer> depthMap;
    private static final HashMap<Integer, HashSet<Tag>> tagMap = new HashMap<>();
    private static final HashMap<Integer, OfficialAccount> accounts = new HashMap<>();
    private static final HashMap<Integer, Integer> articles =
            new HashMap<>();  // <articleId, accountId>>
    private static final HashMap<Integer, Integer> articleContributors = new HashMap<>();

    private int tripleSum;
    private int blockSum;
    private int coupleSum;
    private boolean modifiedCoupleSum;

    public Network() {
        persons = new HashMap<>();
        rootMap = new HashMap<>();
        depthMap = new HashMap<>();
        tripleSum = 0;
        blockSum = 0;
        coupleSum = 0;
        modifiedCoupleSum = false;
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
        if (persons.get(id1).getBestAcquaintance() == id2 ||
                persons.get(id2).getBestAcquaintance() == id1) {
            modifiedCoupleSum = true;
        }
        setTagValueModified(id1, id2);
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
            tripleSum -= persons.get(id1).getAcquaintanceCount() <
                    persons.get(id2).getAcquaintanceCount() ?
                    persons.get(id1).getTripleSum(persons.get(id2)) :
                    persons.get(id2).getTripleSum(persons.get(id1));
            int oldBestAcquaintance1 = persons.get(id1).getBestAcquaintance();
            int oldBestAcquaintance2 = persons.get(id2).getBestAcquaintance();
            persons.get(id1).delLink(persons.get(id2), value);
            persons.get(id2).delLink(persons.get(id1), value);
            if (persons.get(id1).getBestAcquaintance() != oldBestAcquaintance1 ||
                    persons.get(id2).getBestAcquaintance() != oldBestAcquaintance2) {
                modifiedCoupleSum = true;
            }
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
            int oldBestAcquaintance1 = persons.get(id1).getBestAcquaintance();
            int oldBestAcquaintance2 = persons.get(id2).getBestAcquaintance();
            persons.get(id1).modifyLink(persons.get(id2), value);
            persons.get(id2).modifyLink(persons.get(id1), value);
            if (persons.get(id1).getBestAcquaintance() != oldBestAcquaintance1 ||
                    persons.get(id2).getBestAcquaintance() != oldBestAcquaintance2) {
                modifiedCoupleSum = true;
            }
        }
        setTagValueModified(id1, id2);
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
            Person p1 = (Person) getPerson(personId1);
            Tag tag = (Tag) getPerson(personId2).getTag(tagId);
            tag.addPerson(p1);
            tagMap.computeIfAbsent(personId1, k -> new HashSet<>()).add(tag);
        }
    }

    @Override
    public int queryTagValueSum(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!persons.containsKey(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (!persons.get(personId).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }
        return getPerson(personId).getTag(tagId).getValueSum();
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
        tagMap.get(personId1).remove((Tag)(getPerson(personId2).getTag(tagId)));
        if (tagMap.get(personId1).isEmpty()) {
            tagMap.remove(personId1);
        }
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

    @Override
    public int queryCoupleSum() {
        if (!modifiedCoupleSum) {
            return coupleSum;
        }
        // 重构 coupleSum
        coupleSum = 0;
        HashSet<Person> visited = new HashSet<>();
        // 遍历 peopleMap 中的每个 Person
        for (Person myPerson : persons.values()) {
            if (!visited.contains(myPerson)) {
                HashMap<Integer, Person> acquaintance = myPerson.getAcquaintance();
                if (acquaintance.isEmpty()) {
                    continue;
                }
                // 获取最好的朋友对象
                Person bestPerson = acquaintance.get(myPerson.getBestAcquaintance());
                if (bestPerson != null && bestPerson.getBestAcquaintance() == myPerson.getId()) {
                    coupleSum++;
                    visited.add(myPerson);
                    visited.add(bestPerson);
                } else {
                    visited.add(myPerson);
                }
            }
        }
        modifiedCoupleSum = false;
        return coupleSum;
    }

    @Override
    public int queryShortestPath(int id1, int id2)
            throws PersonIdNotFoundException, PathNotFoundException {
        if (!persons.containsKey(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        if (!persons.containsKey(id2)) {
            throw new PersonIdNotFoundException(id2);
        }
        if (!isCircle(id1, id2)) {
            throw new PathNotFoundException(id1, id2);
        }
        return bfs(persons.get(id1), persons.get(id2));
    }

    @Override
    public boolean containsAccount(int id) {
        return accounts.containsKey(id);
    }

    @Override
    public void createOfficialAccount(int personId, int accountId, String name)
            throws PersonIdNotFoundException, EqualOfficialAccountIdException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (containsAccount(accountId)) {
            throw new EqualOfficialAccountIdException(accountId);
        }
        OfficialAccount account = new OfficialAccount(personId, accountId, name);
        account.addFollower(persons.get(personId));
        accounts.put(accountId, account);
    }

    @Override
    public void deleteOfficialAccount(int personId, int accountId)
            throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
            DeleteOfficialAccountPermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        if (accounts.get(accountId).getOwnerId() != personId) {
            throw new DeleteOfficialAccountPermissionDeniedException(personId, accountId);
        }
        accounts.remove(accountId);
    }

    @Override
    public boolean containsArticle(int id) {
        return articles.containsKey(id);
    }

    @Override
    public void contributeArticle(int personId, int accountId, int articleId)
            throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
            EqualArticleIdException, ContributePermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        if (containsArticle(articleId)) {
            throw new EqualArticleIdException(articleId);
        }
        if (!accounts.get(accountId).containsFollower(persons.get(personId))) {
            throw new ContributePermissionDeniedException(personId, articleId);
        }
        // 1. 标记文章已存在
        articles.put(articleId, accountId);
        articleContributors.put(articleId, personId);

        // 2. 将文章添加到 account 中
        accounts.get(accountId).addArticle(persons.get(personId), articleId);

        // 3. 分发给所有 follower（新文章插入到第一个位置）
        for (Person follower : accounts.get(accountId).getFollowers().values()) {
            follower.getReceivedArticles().add(0, articleId);
        }
    }

    @Override
    public void deleteArticle(int personId, int accountId, int articleId)
            throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
            ArticleIdNotFoundException, DeleteArticlePermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        if (!accounts.get(accountId).containsArticle(articleId)) {
            throw new ArticleIdNotFoundException(articleId);
        }
        if (accounts.get(accountId).getOwnerId() != personId) {
            throw new DeleteArticlePermissionDeniedException(personId, articleId);
        }
        OfficialAccount account = accounts.get(accountId);
        int contributorId = account.getArticles().get(articleId);
        int oldContribution = account.getContributions().get(contributorId);

        account.removeArticle(articleId);
        account.getContributions().put(contributorId, oldContribution - 1);
        for (Person follower : account.getFollowers().values()) {
            ArrayList<Integer> oldList = follower.getReceivedArticles();
            ArrayList<Integer> newList = new ArrayList<>();

            for (Integer id : oldList) {
                if (id != articleId) {
                    newList.add(id);
                }
            }

            oldList.clear();
            oldList.addAll(newList);
        }
    }

    @Override
    public void followOfficialAccount(int personId, int accountId)
            throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
            EqualPersonIdException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        if (accounts.get(accountId).containsFollower(getPerson(personId))) {
            throw new EqualPersonIdException(personId);
        }
        accounts.get(accountId).addFollower(getPerson(personId));
    }

    @Override
    public int queryBestContributor(int id) throws OfficialAccountIdNotFoundException {
        if (!containsAccount(id)) {
            throw new OfficialAccountIdNotFoundException(id);
        }
        return accounts.get(id).getBestContributor();
    }

    @Override
    public List<Integer> queryReceivedArticles(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }
        return getPerson(id).queryReceivedArticles();
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

    private int bfs(Person start, Person end) {
        if (start.getId() == end.getId()) {
            return 0;
        }

        Queue<Person> queue = new LinkedList<>();
        HashMap<Person, Integer> distance = new HashMap<>();

        queue.offer(start);
        distance.put(start, 0);

        while (!queue.isEmpty()) {
            Person current = queue.poll();
            int dist = distance.get(current);

            for (Person neighbor : current.getAcquaintance().values()) {
                if (!distance.containsKey(neighbor)) {
                    distance.put(neighbor, dist + 1);
                    if (neighbor == end) {
                        return dist + 1;
                    }
                    queue.offer(neighbor);
                }
            }
        }

        return -1;
    }

    private void setTagValueModified(int id1, int id2) {
        if (tagMap.containsKey(id1)) {
            for (Tag tag : tagMap.get(id1)) {
                tag.setModified(true);
            }
        }
        if (tagMap.containsKey(id2)) {
            for (Tag tag : tagMap.get(id2)) {
                tag.setModified(true);
            }
        }
    }

    public PersonInterface[] getPersons() {
        return persons.values().toArray(new PersonInterface[0]);
    }
}