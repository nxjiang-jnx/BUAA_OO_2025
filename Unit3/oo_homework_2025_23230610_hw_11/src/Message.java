import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

public class Message implements MessageInterface {
    private int id;
    private int socialValue;
    private int type;
    private Person person1;
    private Person person2;
    private Tag tag;

    public Message(int messageId, int messageSocialValue,
        PersonInterface messagePerson1, PersonInterface messagePerson2) {
        this.id = messageId;
        this.type = 0;
        this.tag = null;
        this.socialValue = messageSocialValue;
        this.person1 = (Person) messagePerson1;
        this.person2 = (Person) messagePerson2;
    }

    public Message(int messageId, int messageSocialValue,
        PersonInterface messagePerson1, TagInterface messageTag) {
        this.id = messageId;
        this.type = 1;
        this.tag = (Tag) messageTag;
        this.socialValue = messageSocialValue;
        this.person1 = (Person) messagePerson1;
        this.person2 = null;
    }

    @Override
    public int getType() {
        return this.type;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public int getSocialValue() {
        return this.socialValue;
    }

    @Override
    public PersonInterface getPerson1() {
        return this.person1;
    }

    @Override
    public PersonInterface getPerson2() {
        return this.person2;
    }

    @Override
    public TagInterface getTag() {
        return this.tag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MessageInterface)) {
            return false;
        }
        return (((MessageInterface) obj).getId() == id);
    }
}
