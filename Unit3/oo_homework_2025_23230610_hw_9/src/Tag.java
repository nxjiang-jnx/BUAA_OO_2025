import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface;

import java.util.HashMap;

public class Tag implements TagInterface {
    private final int id;
    private final HashMap<Integer, Person> persons;

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
        if (obj != null && obj instanceof TagInterface) {
            return ((TagInterface) obj).getId() == id;
        } else {
            return false;
        }
    }

    @Override
    public void addPerson(PersonInterface person) {
        persons.put(person.getId(), (Person) person);
    }

    @Override
    public boolean hasPerson(PersonInterface person) {
        return persons.containsKey(person.getId());
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
    }

    @Override
    public int getSize() {
        return persons.size();
    }
}