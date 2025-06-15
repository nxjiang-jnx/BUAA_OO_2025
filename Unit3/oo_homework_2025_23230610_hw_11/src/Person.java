import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Person implements PersonInterface {
    private final int id;
    private final String name;
    private final int age;
    private final HashMap<Integer, Person> acquaintance;
    private final HashMap<Integer, Integer> value;
    private final HashMap<Integer, Tag> tags;
    private final ArrayList<Integer> receivedArticles;

    private int money;
    private int socialValue;
    private final ArrayList<MessageInterface> messages;

    public Person(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        acquaintance = new HashMap<>();
        value = new HashMap<>();
        tags = new HashMap<>();
        receivedArticles = new ArrayList<>();
        money = 0;
        socialValue = 0;
        messages = new ArrayList<>();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public boolean containsTag(int id) {
        return tags.containsKey(id);
    }

    @Override
    public Tag getTag(int id) {
        return tags.getOrDefault(id, null);
    }

    @Override
    public void addTag(TagInterface tag) {
        tags.put(tag.getId(), (Tag) tag);
    }

    @Override
    public void delTag(int id) {
        tags.remove(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof PersonInterface) {
            return ((PersonInterface) obj).getId() == id;
        } else {
            return false;
        }
    }

    @Override
    public boolean isLinked(PersonInterface person) {
        return acquaintance.containsKey(person.getId()) || person.getId() == id;
    }

    @Override
    public int queryValue(PersonInterface person) {
        return value.getOrDefault(person.getId(), 0);
    }

    public void addLink(PersonInterface person, int value) {
        acquaintance.put(person.getId(), (Person) person);
        this.value.put(person.getId(), value);
    }

    public void modifyLink(PersonInterface person, int value) {
        int targetValue = this.value.get(person.getId()) + value;
        this.value.remove(person.getId());
        this.value.put(person.getId(), targetValue);
    }

    public int getTripleSum(PersonInterface person) {
        // 查找共同朋友数量
        int sum = 0;
        for (PersonInterface p : acquaintance.values()) {
            if (person.isLinked(p) && !p.equals(person)) {
                sum++;
            }
        }
        return sum;
    }

    public int getAcquaintanceCount() {
        return acquaintance.size();
    }

    public HashMap<Integer, Person> getAcquaintance() {
        return acquaintance;
    }

    public int getBestAcquaintance() {
        // 返回该人的所有朋友中，亲密度最大的朋友中最小的id
        int maxValue = Integer.MIN_VALUE;
        int bestId = Integer.MAX_VALUE;
        for (HashMap.Entry<Integer, Integer> entry : value.entrySet()) {
            int id = entry.getKey();
            int value = entry.getValue();

            if (value > maxValue || (value == maxValue && id < bestId)) {
                maxValue = value;
                bestId = id;
            }
        }
        return bestId;
    }

    @Override
    public ArrayList<Integer> getReceivedArticles() {
        return receivedArticles;
    }

    @Override
    public ArrayList<Integer> queryReceivedArticles() {
        return new ArrayList<>(receivedArticles.subList(0, Math.min(5, receivedArticles.size())));
    }

    @Override
    public void addSocialValue(int num) {
        socialValue += num;
    }

    @Override
    public int getSocialValue() {
        return socialValue;
    }

    @Override
    public ArrayList<MessageInterface> getMessages() {
        return messages;
    }

    @Override
    public List<MessageInterface> getReceivedMessages() {
        return messages.subList(0, Math.min(5, messages.size()));
    }

    @Override
    public void addMoney(int num) {
        money += num;
    }

    @Override
    public int getMoney() {
        return money;
    }

    public boolean strictEquals(PersonInterface person) { return true; }

    public void delLink(Person person, int value) {
        acquaintance.remove(person.getId());
        this.value.remove(person.getId());
        tags.values().forEach(tag -> {
            if (tag.hasPerson(person)) {
                tag.delPerson(person);
            }
        });
    }

    public void addMessage(Message message) {
        messages.add(0, message);
    }
}