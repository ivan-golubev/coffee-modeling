package net.ivango;

import net.ivango.components.QueueFiller;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Ivan Golubev <igolubev@ea.com> on 4/3/16.
 */
public class CoffeeModelingTest {

    private QueueFiller queueFiller;

    @BeforeClass
    public void init() { queueFiller = new QueueFiller(); }

    @Test
    public void test100() {
        CoffeeModeling cm = new CoffeeModeling();
        cm.launch(queueFiller.generateRandomCustomers(100));
    }

//    @Test
    public void test200() {
        CoffeeModeling cm = new CoffeeModeling();
        cm.launch(queueFiller.generateRandomCustomers(200));
    }

//    @Test
    public void test500() {
        CoffeeModeling cm = new CoffeeModeling();
        cm.launch(queueFiller.generateRandomCustomers(500));
    }

//    @Test
    public void test1000() {
        CoffeeModeling cm = new CoffeeModeling();
        cm.launch(queueFiller.generateRandomCustomers(1000));
    }
}
