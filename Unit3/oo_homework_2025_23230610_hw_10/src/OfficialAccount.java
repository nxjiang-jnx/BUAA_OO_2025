import com.oocourse.spec2.main.OfficialAccountInterface;
import com.oocourse.spec2.main.PersonInterface;

import java.util.HashMap;

public class OfficialAccount implements OfficialAccountInterface {
    private int ownerId;
    private int id;
    private String name;
    private HashMap<Integer, Person> followers;
    private HashMap<Integer, Integer> articles;
    private HashMap<Integer, Integer> contributions;

    public OfficialAccount(int ownerId, int id, String name) {
        this.ownerId = ownerId;
        this.id = id;
        this.name = name;
        followers = new HashMap<>();
        articles = new HashMap<>();
        contributions = new HashMap<>();
    }

    @Override
    public int getOwnerId() {
        return ownerId;
    }

    @Override
    public void addFollower(PersonInterface person) {
        followers.put(person.getId(), (Person) person);
        contributions.put(person.getId(), 0);
    }

    @Override
    public boolean containsFollower(PersonInterface person) {
        return followers.containsKey(person.getId());
    }

    @Override
    public void addArticle(PersonInterface person, int id) {
        articles.put(id, person.getId());
        contributions.put(person.getId(), contributions.getOrDefault(person.getId(), 0) + 1);
    }

    @Override
    public boolean containsArticle(int id) {
        return articles.containsKey(id);
    }

    @Override
    public void removeArticle(int id) {
        articles.remove(id);
    }

    @Override
    public int getBestContributor() {
        int bestId = -1;
        int maxContribution = -1;

        for (Integer personId : followers.keySet()) {
            int currentContribution = contributions.getOrDefault(personId, 0);
            if (currentContribution > maxContribution ||
                (currentContribution == maxContribution && personId < bestId)) {
                maxContribution = currentContribution;
                bestId = personId;
            }
        }

        return bestId;
    }

    public HashMap<Integer, Person> getFollowers() {
        return followers;
    }

    public HashMap<Integer, Integer> getArticles() {
        return articles;
    }

    public HashMap<Integer, Integer> getContributions() {
        return contributions;
    }
}
