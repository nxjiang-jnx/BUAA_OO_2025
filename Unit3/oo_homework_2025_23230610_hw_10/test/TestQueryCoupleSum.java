import com.oocourse.spec2.exceptions.*;
import com.oocourse.spec2.main.PersonInterface;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class TestQueryCoupleSum {
    private Network network;
    private Network oldNetwork;
    private int len;

    public TestQueryCoupleSum(Network network) {
        this.network = network;
        this.oldNetwork = network;
        len = network.getPersons().length;
    }

    @Parameters
    public static Collection prepareData() {
        Random random = new Random();
        int testNum = 500;

        Object[][] object = new Object[testNum][];
        for (int i = 0; i < testNum; i++) {
            Network network = new Network();

            for (int j = 1; j <= 100; j++) {
                try {
                    network.addPerson(new Person(j, String.valueOf(j), j));
                } catch (EqualPersonIdException e) {
                    throw new RuntimeException(e);
                }
            }

            for (int j = 0; j < 1000; j++) {
                int a = random.nextInt(100) + 1;
                int b = random.nextInt(100) + 1;
                int value = random.nextInt(1000) + 1;
                try {
                    network.addRelation(a, b, value);
                } catch (PersonIdNotFoundException | EqualRelationException e) {
                }
            }

            object[i] = new Object[]{network};
        }

        return Arrays.asList(object);
    }

    @Test
    public void testQueryCoupleSum() {
        int sum = 0;
        int testSum = 0;

        PersonInterface[] persons = network.getPersons();
        PersonInterface[] oldPersons = oldNetwork.getPersons();

        for (int i = 0; i < persons.length; i++) {
            for (int j = i + 1; j < persons.length; j++){
                int id1 = 0;
                int id2 = 1;
                try {
                    id1 = network.queryBestAcquaintance(persons[i].getId());
                    id2 = network.queryBestAcquaintance(persons[j].getId());
                } catch (Exception e) {
                }
                if (id1 == persons[j].getId() && id2 == persons[i].getId()) {
                    sum++;
                }
            }
        }

        testSum = network.queryCoupleSum();

        // 检查不变性
        assertEquals(persons.length, len);
        for (int i = 0; i < persons.length; i++) {
            assertTrue(((Person) persons[i]).strictEquals(oldPersons[i]));
        }

        // 检查正确性
        assertEquals(sum, testSum);
    }

    @Test
    public void testQueryCoupleSumEmptyGraph() {
        Network network = new Network();  // 空图，没有任何人或关系
        assertEquals(0, network.queryCoupleSum());  // 应该返回 0 对 couple
    }

    @Test
    public void testQueryCoupleSumSinglePerson() throws EqualPersonIdException {
        Network network = new Network();
        network.addPerson(new Person(1, "Alice", 20));
        assertEquals(0, network.queryCoupleSum());
    }

    @Test
    public void testQueryCoupleSumOneCouple() throws Exception {
        Network network = new Network();
        Person p1 = new Person(1, "A", 10);
        Person p2 = new Person(2, "B", 20);
        network.addPerson(p1);
        network.addPerson(p2);
        network.addRelation(1, 2, 100); // 单一关系，互为最熟

        assertEquals(1, network.queryCoupleSum());
    }

    @Test
    public void testQueryCoupleSumTriangleOneCouple() throws Exception {
        Network network = new Network();
        network.addPerson(new Person(1, "A", 10));
        network.addPerson(new Person(2, "B", 20));
        network.addPerson(new Person(3, "C", 30));

        network.addRelation(1, 2, 50);  // couple
        network.addRelation(1, 3, 10);  // weaker
        network.addRelation(2, 3, 10);  // weaker

        // A 和 B 互为最熟人，C 是次要关系
        assertEquals(1, network.queryCoupleSum());
    }

    @Test
    public void testQueryCoupleSumTwoCouples() throws Exception {
        Network network = new Network();
        for (int i = 1; i <= 4; i++) {
            network.addPerson(new Person(i, "P" + i, 20 + i));
        }

        network.addRelation(1, 2, 100);
        network.addRelation(3, 4, 100);

        // 1-2 和 3-4 互为最熟人
        assertEquals(2, network.queryCoupleSum());
    }

    @Test
    public void testSpecialCouplePatterns() throws EqualPersonIdException, PersonIdNotFoundException, EqualRelationException {
        Person person1 = new Person(1, "A", 21);
        Person person2 = new Person(2, "B", 22);
        Person person3 = new Person(3, "C", 23);
        Person person4 = new Person(4, "D", 24);
        Person person5 = new Person(5, "E", 25);
        Person person6 = new Person(6, "F", 26);   // couple with 5
        Person person7 = new Person(7, "G", 27);   // tie with 8 and 9
        Person person8 = new Person(8, "H", 28);   // tie with 7
        Person person9 = new Person(9, "I", 29);   // tie with 7
        Person person10 = new Person(10, "J", 30); // unlinked node

        Network network1 = new Network();
        network1.addPerson(person1);
        network1.addPerson(person2);
        network1.addPerson(person3);
        network1.addPerson(person4);
        network1.addPerson(person5);
        network1.addPerson(person6);
        network1.addPerson(person7);
        network1.addPerson(person8);
        network1.addPerson(person9);
        network1.addPerson(person10);

        network1.addRelation(1, 2, 100);  // couple
        network1.addRelation(3, 4, 80);   // couple
        network1.addRelation(5, 6, 90);   // couple
        network1.addRelation(7, 8, 40);   // tie
        network1.addRelation(7, 9, 40);   // tie
        network1.addRelation(9, 10, 10);  // one-way, not couple

        Network oldNetwork1 = new Network();
        oldNetwork1.addPerson(person1);
        oldNetwork1.addPerson(person2);
        oldNetwork1.addPerson(person3);
        oldNetwork1.addPerson(person4);
        oldNetwork1.addPerson(person5);
        oldNetwork1.addPerson(person6);
        oldNetwork1.addPerson(person7);
        oldNetwork1.addPerson(person8);
        oldNetwork1.addPerson(person9);
        oldNetwork1.addPerson(person10);

        int peopleLengthBefore = network1.getPersons().length;

        HashMap<Integer, Boolean> ids = new HashMap<>();
        for (int i = 0; i < peopleLengthBefore; i++) {
            ids.put(network1.getPersons()[i].getId(), true);
        }

        HashMap<Integer, HashMap<Integer, Integer>> values = new HashMap<>();
        for (int i = 0; i < peopleLengthBefore; i++) {
            values.put(network1.getPersons()[i].getId(), new HashMap<>());
            for (int j = 0; j < peopleLengthBefore; j++) {
                values.get(network1.getPersons()[i].getId()).put(network1.getPersons()[j].getId(), network1.getPersons()[i].queryValue(network1.getPersons()[j]));
            }
        }

        int result = network1.queryCoupleSum();

        PersonInterface[] persons = network1.getPersons();
        int peopleLengthAfter = network1.getPersons().length;

        Person[] after = new Person[10];
        for (int i = 0; i < persons.length; i++) {
            after[persons[i].getId() - 1] = (Person) persons[i];
        }

        for (int i = 0; i < 10; i++) {
            assertEquals(i + 1, after[i].getId());
            assertEquals(String.valueOf((char) ('A' + i)), after[i].getName());
            assertEquals(21 + i, after[i].getAge());
        }

        assertTrue(after[0].isLinked(after[1]));
        assertTrue(after[1].isLinked(after[0]));
        assertTrue(after[2].isLinked(after[3]));
        assertTrue(after[3].isLinked(after[2]));
        assertTrue(after[4].isLinked(after[5]));
        assertTrue(after[5].isLinked(after[4]));
        assertTrue(after[6].isLinked(after[7]));
        assertTrue(after[6].isLinked(after[8]));
        assertTrue(after[8].isLinked(after[9]));

        assertEquals(4, result);
        assertEquals(peopleLengthBefore, peopleLengthAfter);

        for (int i = 0; i < peopleLengthAfter; i++) {
            assertTrue(ids.containsKey(network1.getPersons()[i].getId()));
        }

        for (int i = 0; i < peopleLengthAfter; i++) {
            assertTrue(values.containsKey(network1.getPersons()[i].getId()));
            HashMap<Integer, Integer> tmpMap = values.get(network1.getPersons()[i].getId());
            for (int j = 0; j < peopleLengthAfter; j++) {
                assertTrue(tmpMap.containsKey(network1.getPersons()[j].getId()));
                assertEquals(tmpMap.get(network1.getPersons()[j].getId()).intValue(),
                        network1.getPersons()[i].queryValue(network1.getPersons()[j]));
            }
        }

        assertEquals(10, network1.getPersons().length);

        for (int i = 0; i < network1.getPersons().length; i++) {
            boolean exist = false;
            for (int j = 0; j < oldNetwork1.getPersons().length; j++) {
                if (network1.getPersons()[i].getId() == oldNetwork1.getPersons()[j].getId()) {
                    exist = true;
                    assertTrue(((Person) network1.getPersons()[i]).strictEquals(oldNetwork1.getPersons()[j]));
                }
            }
            assertTrue(exist);
        }
    }
}
