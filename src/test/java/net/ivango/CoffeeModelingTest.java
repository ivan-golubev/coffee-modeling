package net.ivango;

import net.ivango.components.QueueFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Launches coffee-drinking scenarios with different customer queue capacity.
 * A separate html report is generated for each scenario.
 *
 * Created by Ivan Golubev <igolubev@ea.com> on 4/3/16.
 */
public class CoffeeModelingTest {

    private QueueFiller queueFiller;

    private Logger logger = LoggerFactory.getLogger(CoffeeModelingTest.class);

    @BeforeClass
    public void init() { queueFiller = new QueueFiller(); }

    @Test
    public void test100() {
        CoffeeModeling cm = new CoffeeModeling();
        int count = 100;
        logger.info("Running test, programmers count: " + count);
        cm.launch(queueFiller.generateRandomCustomers(count));
    }

    @Test
    public void test200() {
        CoffeeModeling cm = new CoffeeModeling();
        int count = 200;
        logger.info("Running test, programmers count: " + count);
        cm.launch(queueFiller.generateRandomCustomers(count));
    }

    @Test
    public void test500() {
        CoffeeModeling cm = new CoffeeModeling();
        int count = 500;
        logger.info("Running test, programmers count: " + count);
        cm.launch(queueFiller.generateRandomCustomers(count));
    }

    @Test
    public void test1000() {
        CoffeeModeling cm = new CoffeeModeling();
        int count = 1000;
        logger.info("Running test, programmers count: " + count);
        cm.launch(queueFiller.generateRandomCustomers(count));
    }
}
