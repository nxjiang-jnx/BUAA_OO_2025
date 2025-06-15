import com.oocourse.spec3.exceptions.EqualPersonIdException;
import com.oocourse.spec3.exceptions.EqualRelationException;
import com.oocourse.spec3.exceptions.PersonIdNotFoundException;
import com.oocourse.spec3.exceptions.RelationNotFoundException;
import com.oocourse.spec3.exceptions.EqualTagIdException;
import com.oocourse.spec3.exceptions.TagIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualArticleIdException;
import com.oocourse.spec3.exceptions.ArticleIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualOfficialAccountIdException;
import com.oocourse.spec3.exceptions.OfficialAccountIdNotFoundException;
import com.oocourse.spec3.exceptions.PathNotFoundException;
import com.oocourse.spec3.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec3.exceptions.DeleteOfficialAccountPermissionDeniedException;
import com.oocourse.spec3.exceptions.ContributePermissionDeniedException;
import com.oocourse.spec3.exceptions.DeleteArticlePermissionDeniedException;
import com.oocourse.spec3.exceptions.MessageIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualMessageIdException;
import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualEmojiIdException;
import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.NetworkInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;
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
    private static final HashMap<Integer, Integer> articles = new HashMap<>();
    private static final HashMap<Integer, Integer> articleContributors = new HashMap<>();
    private static final HashMap<Integer, Message> messages = new HashMap<>();
    private static final HashMap<Integer, Integer> emojiMap = new HashMap<>();
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
            HashSet<Integer> id1Set = new HashSet<>();
            dfs(id1, id1Set);
            if (!id1Set.contains(id2)) {
                HashSet<Integer> id2Set = new HashSet<>();
                dfs(id2, id2Set);
                for (Integer id : id2Set) {
                    rootMap.put(id, id2);
                    depthMap.put(id, 1);
                }
                blockSum++;
                depthMap.put(id2, 2);
            }
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
    public boolean containsMessage(int id) {
        return messages.containsKey(id);
    }

    @Override
    public void addMessage(MessageInterface message) throws EqualMessageIdException,
        EmojiIdNotFoundException, EqualPersonIdException, ArticleIdNotFoundException {
        if (containsMessage(message.getId())) {
            throw new EqualMessageIdException(message.getId());
        }
        if ((message instanceof EmojiMessage) &&
            !containsEmojiId(((EmojiMessage) message).getEmojiId())) {
            throw new EmojiIdNotFoundException(((EmojiMessage) message).getEmojiId());
        }
        if ((message instanceof ForwardMessage) &&
            !containsArticle(((ForwardMessage) message).getArticleId())) {
            throw new ArticleIdNotFoundException(((ForwardMessage) message).getArticleId());
        }
        if ((message instanceof ForwardMessage) && !(message.getPerson1().getReceivedArticles().
            contains(((ForwardMessage) message).getArticleId()))) {
            throw new ArticleIdNotFoundException(((ForwardMessage) message).getArticleId());
        }
        if (message.getType() == 0 && message.getPerson1().equals(message.getPerson2())) {
            throw new EqualPersonIdException(message.getPerson1().getId());
        }
        messages.put(message.getId(), (Message) message);
    }

    @Override
    public MessageInterface getMessage(int id) {
        return messages.getOrDefault(id, null);
    }

    @Override
    public void sendMessage(int id) throws RelationNotFoundException,
        MessageIdNotFoundException, TagIdNotFoundException {
        if (!containsMessage(id)) {
            throw new MessageIdNotFoundException(id);
        }
        if (getMessage(id).getType() == 0 &&
            !(getMessage(id).getPerson1().isLinked(getMessage(id).getPerson2()))) {
            throw new RelationNotFoundException(getMessage(id).getPerson1().getId(),
            getMessage(id).getPerson2().getId());
        }
        if (getMessage(id).getType() == 1 &&
            !getMessage(id).getPerson1().containsTag(getMessage(id).getTag().getId())) {
            throw new TagIdNotFoundException(getMessage(id).getTag().getId());
        }

        if (getMessage(id).getType() == 0 &&
            getMessage(id).getPerson1() != getMessage(id).getPerson2()) {
            Message message = messages.get(id);
            Person person1 = (Person) (message.getPerson1());
            Person person2 = (Person) (message.getPerson2());
            person1.addSocialValue(message.getSocialValue());
            person2.addSocialValue(message.getSocialValue());
            if (message instanceof RedEnvelopeMessage) {
                person1.addMoney(-((RedEnvelopeMessage) message).getMoney());
                person2.addMoney(((RedEnvelopeMessage) message).getMoney());
            } else if (message instanceof ForwardMessage) {
                int articleId = ((ForwardMessage) message).getArticleId();
                person2.getReceivedArticles().add(0, articleId);
            } else if (message instanceof EmojiMessage) {
                int emojiId = ((EmojiMessage) message).getEmojiId();
                emojiMap.replace(emojiId, emojiMap.get(emojiId) + 1);
            }
            person2.addMessage(message);
        } else if (getMessage(id).getType() == 1) {
            Message message = messages.get(id);
            Person person1 = (Person) (message.getPerson1());
            person1.addSocialValue(message.getSocialValue());
            Tag tag = (Tag) (message.getTag());
            tag.addSocialValue(message.getSocialValue());
            if (message instanceof RedEnvelopeMessage && tag.getSize() > 0) {
                int size = tag.getSize();
                int average = ((RedEnvelopeMessage) message).getMoney() / size;
                person1.addMoney(-average * size);
                tag.addMoney(average);
            } else if (message instanceof ForwardMessage && tag.getSize() > 0) {
                int articleId = ((ForwardMessage) message).getArticleId();
                tag.addArticle(articleId);
            } else if (message instanceof EmojiMessage) {
                int emojiId = ((EmojiMessage) message).getEmojiId();
                emojiMap.replace(emojiId, emojiMap.get(emojiId) + 1);
            }
            tag.addMessage(message);
        }
        messages.remove(id);
    }

    @Override
    public int querySocialValue(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }
        return persons.get(id).getSocialValue();
    }

    @Override
    public List<MessageInterface> queryReceivedMessages(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }
        return persons.get(id).getReceivedMessages();
    }

    @Override
    public boolean containsEmojiId(int id) { return emojiMap.containsKey(id); }

    @Override
    public void storeEmojiId(int id) throws EqualEmojiIdException {
        if (containsEmojiId(id)) {
            throw new EqualEmojiIdException(id);
        }
        emojiMap.put(id, 0);
    }

    @Override
    public int queryMoney(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }
        return persons.get(id).getMoney();
    }

    @Override
    public int queryPopularity(int id) throws EmojiIdNotFoundException {
        if (!containsEmojiId(id)) {
            throw new EmojiIdNotFoundException(id);
        }
        return emojiMap.get(id);
    }

    @Override
    public int deleteColdEmoji(int limit) {
        messages.entrySet().removeIf(entry -> entry.getValue() instanceof EmojiMessage
            && emojiMap.get(((EmojiMessage) entry.getValue()).getEmojiId()) < limit);
        emojiMap.entrySet().removeIf(entry -> entry.getValue() < limit);
        return emojiMap.size();
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
        coupleSum = 0;
        HashSet<Person> visited = new HashSet<>();
        for (Person myPerson : persons.values()) {
            if (!visited.contains(myPerson)) {
                HashMap<Integer, Person> acquaintance = myPerson.getAcquaintance();
                if (acquaintance.isEmpty()) {
                    continue;
                }
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
    public boolean containsAccount(int id) { return accounts.containsKey(id); }

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
        articles.put(articleId, accountId);
        articleContributors.put(articleId, personId);
        accounts.get(accountId).addArticle(persons.get(personId), articleId);
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
            for (Tag tag : tagMap.get(id1)) { tag.setModified(true); }
        }
        if (tagMap.containsKey(id2)) {
            for (Tag tag : tagMap.get(id2)) { tag.setModified(true); }
        }
    }

    public PersonInterface[] getPersons() {
        return persons.values().toArray(new PersonInterface[0]);
    }

    public MessageInterface[] getMessages() { return messages.values().toArray(new Message[0]); }

    public int[] getEmojiIdList() {
        return emojiMap.keySet().stream().mapToInt(Integer::intValue).toArray(); }

    public int[] getEmojiHeatList() {
        return emojiMap.values().stream().mapToInt(Integer::intValue).toArray(); }
}