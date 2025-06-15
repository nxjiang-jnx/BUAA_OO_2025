import com.oocourse.spec3.main.EmojiMessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

public class EmojiMessage extends Message implements EmojiMessageInterface {
    private final int emojiId;

    public EmojiMessage(int messageId, int emojiNumber,
        PersonInterface messagePerson1, PersonInterface messagePerson2) {
        super(messageId, emojiNumber, messagePerson1, messagePerson2);
        this.emojiId = emojiNumber;
    }

    public EmojiMessage(int messageId, int emojiNumber,
        PersonInterface messagePerson1, TagInterface messageTag) {
        super(messageId, emojiNumber, messagePerson1, messageTag);
        this.emojiId = emojiNumber;
    }

    @Override
    public int getEmojiId() {
        return emojiId;
    }
}
