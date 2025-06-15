import com.oocourse.spec3.exceptions.*;
import com.oocourse.spec3.main.MessageInterface;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.*;

import java.util.*;

@RunWith(Parameterized.class)
public class TestDeleteColdEmoji {
    private Network network;
    private int limit;

    public TestDeleteColdEmoji(Network network, int limit) {
        this.network = network;
        this.limit = limit;
    }

    @Parameters
    public static Collection prepareData() {
        Random random = new Random();
        int testNum = 1000;
        Object[][] object = new Object[testNum][];

        for (int i = 0; i < testNum; i++) {
            Network network = new Network();
            int limit = random.nextInt(4);

            try {
                network.addPerson(new Person(1, "a", 11));
                network.addPerson(new Person(2, "b", 22));
                network.addPerson(new Person(3, "c", 33));
                network.addPerson(new Person(-1, "Tom", 1));
                network.addPerson(new Person(-2, "Jerry", 2));
            } catch (EqualPersonIdException e) {
            }

            try {
                network.addRelation(1, 2, 10);
                network.addRelation(1, 3, 10);
                network.addRelation(2, 3, 10);
                network.addRelation(-1, -2, 1);
                network.addTag(-1, new Tag(-3));
                network.addTag(1, new Tag(1));
                network.addPersonToTag(2, 1, 1);
                network.addPersonToTag(3, 1, 1);
                network.addPersonToTag(-1, 1, 1);
            } catch (Exception e) {
            }

            for (int j = 1; j <= 100; j++) {
                try {
                    network.storeEmojiId(j);
                } catch (Exception e) {
                }
            }

            for (int j = 1; j <= 200; j++) {
                int idx = random.nextInt(4) + 1;
                int messageType = random.nextInt(2);

                if (idx == 1 && messageType == 0) {
                    EmojiMessage emojiMessage = new EmojiMessage(j, random.nextInt(1000) + 1, network.getPerson(1), network.getPerson(random.nextInt(2) + 2));
                    try {
                        network.addMessage(emojiMessage);
                    } catch (Exception e) {
                    }
                    EmojiMessage emojiMessage1 = new EmojiMessage(j, random.nextInt(1000) + 1, network.getPerson(1), network.getPerson(random.nextInt(2) + 2));
                    try {
                        network.addMessage(emojiMessage1);
                    } catch (Exception e) {
                    }
                    continue;
                }
                if (idx == 1) {
                    EmojiMessage emojiMessage = new EmojiMessage(j, random.nextInt(1000) + 1, network.getPerson(1), network.getPerson(1).getTag(1));
                    try {
                        network.addMessage(emojiMessage);
                    } catch (Exception e) {
                    }
                    continue;
                }
                if (idx == 2 && messageType == 0) {
                    RedEnvelopeMessage redEnvelopeMessage = new RedEnvelopeMessage(j, j, network.getPerson(1), network.getPerson(random.nextInt(2) + 2));
                    try {
                        network.addMessage(redEnvelopeMessage);
                    } catch (Exception e) {
                    }
                    RedEnvelopeMessage redEnvelopeMessage1 = new RedEnvelopeMessage(j, j, network.getPerson(1), network.getPerson(random.nextInt(2) - 2));
                    try {
                        network.addMessage(redEnvelopeMessage1);
                    } catch (Exception e) {
                    }
                    continue;
                }
                if (idx == 2) {
                    RedEnvelopeMessage redEnvelopeMessage = new RedEnvelopeMessage(j, j, network.getPerson(1), network.getPerson(1).getTag(1));
                    try {
                        network.addMessage(redEnvelopeMessage);
                    } catch (Exception e) {
                    }
                    continue;
                }
                if (idx == 3 && messageType == 0) {
                    ForwardMessage forwardMessage = new ForwardMessage(j, j, network.getPerson(1), network.getPerson(random.nextInt(2) + 2));
                    try {
                        network.addMessage(forwardMessage);
                    } catch (Exception e) {
                    }
                    ForwardMessage forwardMessage1 = new ForwardMessage(j, j, network.getPerson(1), network.getPerson(random.nextInt(2) - 2));
                    try {
                        network.addMessage(forwardMessage1);
                    } catch (Exception e) {
                    }
                    continue;
                }
                if (idx == 3) {
                    ForwardMessage forwardMessage = new ForwardMessage(j, j, network.getPerson(1), network.getPerson(1).getTag(1));
                    try {
                        network.addMessage(forwardMessage);
                    } catch (Exception e) {
                    }
                    continue;
                }
                Message message;
                if (messageType == 0) {
                    message = new Message(j, j, network.getPerson(1), network.getPerson(random.nextInt(2) + 2));
                } else {
                    message = new Message(j, j, network.getPerson(1), network.getPerson(1).getTag(1));
                }
                try {
                    network.addMessage(message);
                } catch (Exception e) {
                }
                Message message1 = new Message(j, j, network.getPerson(1), network.getPerson(random.nextInt(2) - 2));
                try {
                    network.addMessage(message1);
                } catch (Exception e) {
                }
            }
            object[i] = new Object[]{network, limit};
        }
        return Arrays.asList(object);
    }

    @Test
    public void testDeleteColdEmoji() {
        int sum = 0;
        int testSum = 0;
        int len = 0;
        int testLen = 0;

        MessageInterface[] testMessages = network.getMessages();
        MessageInterface[] messages = new Message[testMessages.length];

        for (MessageInterface message : testMessages) {
            int messageId = message.getId();
            int type = message.getType();
            int socialValue = message.getSocialValue();
            Tag tag = (Tag) message.getTag();
            Person person1 = (Person) message.getPerson1();
            Person person2 = (Person) message.getPerson2();

            if (message instanceof EmojiMessage) {
                if (type == 0) {
                    messages[testLen++] = new EmojiMessage(messageId, socialValue, person1, person2);
                } else {
                    messages[testLen++] = new EmojiMessage(messageId, socialValue, person1, tag);
                }
            } else if (message instanceof RedEnvelopeMessage) {
                int money = ((RedEnvelopeMessage) message).getMoney();
                if (type == 0) {
                    messages[testLen++] = new RedEnvelopeMessage(messageId, money, person1, person2);
                } else {
                    messages[testLen++] = new RedEnvelopeMessage(messageId, money, person1, tag);
                }
            } else if (message instanceof ForwardMessage) {
                int articleId = ((ForwardMessage) message).getArticleId();
                if (type == 0) {
                    messages[testLen++] = new ForwardMessage(messageId, articleId, person1, person2);
                } else {
                    messages[testLen++] = new ForwardMessage(messageId, articleId, person1, tag);
                }
            } else {
                if (type == 0) {
                    messages[testLen++] = new Message(messageId, socialValue, person1, person2);
                } else {
                    messages[testLen++] = new Message(messageId, socialValue, person1, tag);
                }
            }
        }
        int[] emojiIdList = network.getEmojiIdList();
        int[] emojiHeatList = network.getEmojiHeatList();

        // 测试对比
        int[] testEmojiIdList = new int[emojiIdList.length];
        int[] testEmojiHeatList = new int[emojiHeatList.length];

        MessageInterface[] shadowMessages = new Message[messages.length];

        for (int i = 0; i < emojiIdList.length; i++) {
            if (emojiHeatList[i] >= limit) {
                testEmojiIdList[sum] = emojiIdList[i];
                testEmojiHeatList[sum] = emojiHeatList[i];
                sum++;
            }
        }

        Map<Integer, Integer> emojiHeatMap = new HashMap<>();
        for (int i = 0; i < emojiIdList.length; i++) {
            emojiHeatMap.put(emojiIdList[i], emojiHeatList[i]);
        }

        for (MessageInterface message : messages) {
            if (message instanceof EmojiMessage) {
                int emojiId = ((EmojiMessage) message).getEmojiId();
                Integer heat = emojiHeatMap.get(emojiId);
                if (heat != null && heat >= limit) {
                    shadowMessages[len++] = message;
                }
            } else {
                shadowMessages[len++] = message;
            }
        }

        // 检查正确性
        testSum = network.deleteColdEmoji(limit);
        assertEquals(testSum, sum);

        // 检查不变性
        // 先看长度
        assertEquals(sum, network.getEmojiIdList().length);
        assertEquals(sum, network.getEmojiHeatList().length);
        assertEquals(len, network.getMessages().length);

        // 再看顺序对应
        for (int i = 0; i < sum; i++) {
            assertEquals(testEmojiIdList[i], network.getEmojiIdList()[i]);
            assertEquals(testEmojiHeatList[i], network.getEmojiHeatList()[i]);
        }

        for (int i = 0; i < len; i++) {
            assertEquals(shadowMessages[i].getClass(), network.getMessages()[i].getClass());
            assertEquals(shadowMessages[i].getId(), network.getMessages()[i].getId());
            assertEquals(shadowMessages[i].getType(), network.getMessages()[i].getType());
            assertEquals(shadowMessages[i].getSocialValue(), network.getMessages()[i].getSocialValue());
            assertEquals(shadowMessages[i].getTag(), network.getMessages()[i].getTag());
            assertEquals(shadowMessages[i].getPerson1(), network.getMessages()[i].getPerson1());
            assertEquals(shadowMessages[i].getPerson2(), network.getMessages()[i].getPerson2());
            if (shadowMessages[i] instanceof EmojiMessage) {
                assertEquals(((EmojiMessage) (shadowMessages[i])).getEmojiId(), ((EmojiMessage) (network.getMessages()[i])).getEmojiId());
            } else if (shadowMessages[i] instanceof ForwardMessage) {
                assertEquals(((ForwardMessage) shadowMessages[i]).getArticleId(), ((ForwardMessage) (network.getMessages()[i])).getArticleId());
            } else if (shadowMessages[i] instanceof RedEnvelopeMessage) {
                assertEquals(((RedEnvelopeMessage) (shadowMessages[i])).getMoney(), ((RedEnvelopeMessage) (network.getMessages()[i])).getMoney());
            }
        }
    }

}