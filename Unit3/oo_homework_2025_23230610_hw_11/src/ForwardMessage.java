import com.oocourse.spec3.main.ForwardMessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

public class ForwardMessage extends Message implements ForwardMessageInterface {
    private int articleId;

    public ForwardMessage(int messageId, int article,
        PersonInterface messagePerson1, PersonInterface messagePerson2) {
        super(messageId, Math.abs(article) % 200, messagePerson1, messagePerson2);
        this.articleId = article;
    }

    public ForwardMessage(int messageId, int article,
        PersonInterface messagePerson1, TagInterface messageTag) {
        super(messageId, Math.abs(article) % 200, messagePerson1, messageTag);
        this.articleId = article;
    }

    @Override
    public int getArticleId() {
        return articleId;
    }
}
