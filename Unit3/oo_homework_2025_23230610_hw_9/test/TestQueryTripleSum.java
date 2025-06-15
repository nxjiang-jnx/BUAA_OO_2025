import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.main.PersonInterface;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class TestQueryTripleSum {
    private Network network;
    private Network oldNetwork;
    private int len;

    public TestQueryTripleSum(Network network) {
        this.network = network;
        this.oldNetwork = network;
        this.len = network.getPersons().length;
    }

    @Parameters
    public static Collection prepareData() {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        int testNum = 10;

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

            for (int j = 0; j < 500; j++) {
                int a = random.nextInt(100) + 1;
                int b = random.nextInt(100) + 1;
                try {
                    network.addRelation(a, b, 10);
                } catch (PersonIdNotFoundException | EqualRelationException e) {
                }
            }

            object[i] = new Object[]{network};
        }

        return Arrays.asList(object);
    }

    @Test
    public void testQueryTripleSum() {
        int sum = 0;
        int testSum = 0;

        PersonInterface[] persons = network.getPersons();

        for (int i = 0; i < persons.length; i++) {
            for (int j = i + 1; j < persons.length; j++) {
                for (int k = j + 1; k < persons.length; k++) {
                    if (network.getPerson(persons[i].getId()).isLinked(network.getPerson(persons[j].getId()))
                            && network.getPerson(persons[j].getId()).isLinked(network.getPerson(persons[k].getId()))
                            && network.getPerson(persons[i].getId()).isLinked(network.getPerson(persons[k].getId()))) {
                        sum++;
                    }
                }
            }
        }

        testSum = network.queryTripleSum();
        assertEquals(sum, testSum);
        assertEquals(persons.length, len);
        for (int i = 0; i < persons.length; i++) {
            assertTrue(((Person)persons[i]).strictEquals(oldNetwork.getPerson(persons[i].getId())));
        }
    }

}
