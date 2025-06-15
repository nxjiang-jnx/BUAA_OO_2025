import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

import java.util.HashMap;

public class Tag implements TagInterface {
    private final int id;
    private final HashMap<Integer, Person> persons;

    // 缓存字段
    private int lastValueSum = 0;
    private boolean modified = true;

    public Tag(int id) {
        this.id = id;
        persons = new HashMap<>();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TagInterface && ((TagInterface) obj).getId() == id;
    }

    @Override
    public void addPerson(PersonInterface person) {
        persons.put(person.getId(), (Person) person);
        modified = true;
    }

    @Override
    public boolean hasPerson(PersonInterface person) {
        return persons.containsKey(person.getId());
    }

    @Override
    public int getValueSum() {
        if (!modified) {
            return lastValueSum;
        }

        int sum = 0;
        for (Person person : persons.values()) {
            for (Person acquaintance : person.getAcquaintance().values()) {
                if (hasPerson(acquaintance)) {
                    sum += person.queryValue(acquaintance);
                }
            }
        }

        lastValueSum = sum;
        modified = false;
        return sum;
    }

    @Override
    public int getAgeMean() {
        return persons.isEmpty() ? 0 : persons.values().stream().
            mapToInt(Person::getAge).sum() / persons.size();
    }

    @Override
    public int getAgeVar() {
        if (persons.isEmpty()) {
            return 0;
        }

        int mean = getAgeMean();
        int sum = 0;

        for (Person p : persons.values()) {
            sum += (p.getAge() - mean) * (p.getAge() - mean);
        }

        return sum / persons.size();
    }

    @Override
    public void delPerson(PersonInterface person) {
        persons.remove(person.getId());
        modified = true;
    }

    @Override
    public int getSize() {
        return persons.size();
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public void addSocialValue(int value) {
        for (Person person : persons.values()) {
            person.addSocialValue(value);
        }
    }

    public void addMoney(int money) {
        for (Person person : persons.values()) {
            person.addMoney(money);
        }
    }

    public void addArticle(int articleId) {
        for (Person person : persons.values()) {
            person.getReceivedArticles().add(0, articleId);
        }
    }

    public void addMessage(Message message) {
        for (Person person : persons.values()) {
            person.addMessage(message);
        }
    }
}