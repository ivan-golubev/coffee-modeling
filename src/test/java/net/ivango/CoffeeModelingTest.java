package net.ivango;

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
        CoffeeModeling cm = new CoffeeModeling(queueFiller.generateRandomCustomers(100));
        cm.launch();
    }

    @Test
    public void test200() {
        CoffeeModeling cm = new CoffeeModeling(queueFiller.generateRandomCustomers(200));
        cm.launch();
    }

    @Test
    public void test500() {
        CoffeeModeling cm = new CoffeeModeling(queueFiller.generateRandomCustomers(500));
        cm.launch();
    }

    @Test
    public void test1000() {
        CoffeeModeling cm = new CoffeeModeling(queueFiller.generateRandomCustomers(1000));
        cm.launch();
    }
}
