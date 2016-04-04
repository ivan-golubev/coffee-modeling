package net.ivango;

import net.ivango.components.CashRegister;
import net.ivango.components.CoffeeMachine;
import net.ivango.config.Config;
import net.ivango.entities.Cup;
import net.ivango.entities.Programmer;
import net.ivango.metrics.EventProcessor;
import net.ivango.metrics.events.CustomerServiced;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static net.ivango.config.Properties.*;

/**
 * Models the coffee-drinking process:
 * a queue of customers (programmers) chooses coffee type to drink,
 * then pays for it, then pour the drink and leaves.
 *
 * An html report is generated as a result of this execution.
 * An instance of this class is not reusable after calling the launch() method.
 *
 * Created by Ivan Golubev <igolubev@ea.com> on 4/3/16.
 */
public class CoffeeModeling {

    /* Thread pools and queues to run the simulation */
    private ExecutorService coffeeSelectionThreadPool, paymentThreadPool, pickCoffeeThreadPool;
    private CountDownLatch doneSignal;
    private BlockingQueue<Programmer> paymentQueue = new LinkedBlockingDeque<>(),
                                      dispenseQueue = new LinkedBlockingDeque<>();

    /* event aggregator/ processor */
    private EventProcessor eventProcessor;
    /* components to emulate the coffee-drinking process */
    private CashRegister cashRegister;
    private CoffeeMachine[] coffeeMachines;

    /* miscellaneous objects */
    private Logger logger = LoggerFactory.getLogger(CoffeeModeling.class);
    private Random random = new Random();

    public CoffeeModeling() {
        /* read configuration setting from external JSON file */
        Config.init();

        /* initialize the components */
        eventProcessor = new EventProcessor();
        cashRegister = new CashRegister(eventProcessor);
        /* initialize the coffee machines: each machine should have a number (to name it later in reports) */
        coffeeMachines = new CoffeeMachine[Config.get(PICK_COFFEE_PARALLELISM)];
        for (int i=0; i < Config.get(PICK_COFFEE_PARALLELISM); i++) { coffeeMachines[i] = new CoffeeMachine(eventProcessor, i+1); }

        /* initialize the worker thread pools */
        coffeeSelectionThreadPool = Executors.newFixedThreadPool(Config.get(COFFEE_SELECT_PARALLELISM));
        paymentThreadPool = Executors.newFixedThreadPool(Config.get(PAYMENT_PARALLELISM));
        pickCoffeeThreadPool = Executors.newFixedThreadPool(Config.get(PICK_COFFEE_PARALLELISM));

        /* initialize the tasks */
        for (int i=0; i < Config.get(PAYMENT_PARALLELISM); i++) { paymentThreadPool.submit( paymentTask() ); }
        for (int i=0; i < Config.get(PICK_COFFEE_PARALLELISM); i++) { pickCoffeeThreadPool.submit( pickCoffeeTask() ); }
    }

    /**
     * Launches the simulation.
     * Each customer (programmer) is serviced.
     * As a result an html report with statistics is generated.
     *
     * @param arrivedCustomers – list of all customers to process.
     * */
    public void launch(List<Programmer> arrivedCustomers) {
        try {
            doneSignal = new CountDownLatch( arrivedCustomers.size() );
            /* 1. First all the programmers select their favourite coffee */
            coffeeSelectionThreadPool.invokeAll(
                    arrivedCustomers.stream().map(this::selectCoffeeTask).collect(Collectors.toList())
            );
            /* initiate an orderly shutdown after finishing the previously submitted tasks */
            coffeeSelectionThreadPool.shutdown();
            /* 2. Then they pay for it using cash or a credit card – done via worker threads plus blocking queue */
            /* 3. Then they pick the coffee and leave - done via worker threads plus blocking queue */

            /* wait for the processing to finish */
            doneSignal.await();
            /* do a graceful shutdown */
            paymentThreadPool.shutdown();
            pickCoffeeThreadPool.shutdown();

            /* Once all the work is complete - gather the stats */
            eventProcessor.processEvents();
        } catch (Exception e) {
            logger.error("Unexpected error during execution: ", e);
        } finally {
            /* force shutdown if required */
            shutdown();
        }
    }

    /**
     * Cleans up all the resources (thread pools) if necessary.
     * */
    private void shutdown(){
        if ( !coffeeSelectionThreadPool.isTerminated() ) {
            coffeeSelectionThreadPool.shutdownNow();
        }
        if ( !paymentThreadPool.isTerminated() ) {
            paymentThreadPool.shutdownNow();
        }
        if ( !pickCoffeeThreadPool.isTerminated() ) {
            pickCoffeeThreadPool.shutdownNow();
        }
    }

    /**
     * @return a task which emulates a coffee selection process
     * and then forwards this customer to a payment queue to pay for the coffee.
     *
     * @param programmer – a customer to process.
     * */
    private Callable<Void> selectCoffeeTask(Programmer programmer) {
        return () -> {
            /* save the processing start timestamp for this customer for later use*/
            programmer.markServiceStart();

            logger.debug("Selecting coffee...");
            programmer.selectFavouriteCoffee( CoffeeMachine.getCoffeeTypes() );
            /* send him to the payment queue */
            paymentQueue.offer(programmer);
            return null;
        };
    }

    /**
     * @return a task which emulates a payment procedure:
     * a customer is taken from a queue, then after a payment
     * he/she is forwarded to a coffee machine (dispense queue)
     * to pour a coffee.
     *
     * This task is supposed to be used as a worker thread, which will operate until interrupted.
     * */
    private Runnable paymentTask() {
        return () -> {
            try {
                while ( !Thread.interrupted() ) {

                    /* take next guy from the payment queue */
                    Programmer programmer = paymentQueue.take();

                    /* the payment operation itself */
                    logger.debug("Paying...");
                    cashRegister.pay( programmer.getPaymentType() );

                    /* send this guy to a dispense queue */
                    dispenseQueue.offer( programmer );
                }
            } catch (InterruptedException ie) {
                logger.info("Payment worker thread interrupted.");
            }
            logger.info("Payment worker thread stopped.");
        };
    }

    /**
     * @return a task which emulates the final step for a customer:
     * pick a cup, pour coffee and leave.
     *
     * This task is supposed to be used as a worker thread, which will operate until interrupted.
     * */
    private Runnable pickCoffeeTask() {
        return () -> {
            try {
                while ( !Thread.interrupted() ) {

                    /* take next guy from the dispense queue */
                    Programmer programmer = dispenseQueue.take();

                    logger.debug("Picking coffee...");
                    /* 1. First - the programmer looks for a cup */
                    Cup cup = programmer.findCup();

                    /* 2. Then he puts it under the outlet, pick the type of coffee he paid for and waits till the cup is filled */
                    CoffeeMachine coffeeMachine = coffeeMachines[random.nextInt(coffeeMachines.length)];
                    cup = coffeeMachine.pourCoffee( cup, programmer.getSelectedCoffee() );

                    logger.debug("Leaving...");
                    /* 3. Finally he takes the cup and leaves */
                    programmer.takeTheCupAndLeave( cup );

                    /* mark the completion of this task */
                    doneSignal.countDown();

                    /* submit an event: customer services – for a later report */
                    eventProcessor.submitEvent(new CustomerServiced(programmer.getServiceStart(), DateTime.now()));
                }
            } catch (InterruptedException ie) {
                logger.info("Pick Coffee worker thread interrupted.");
            }
            logger.info("Pick Coffee worker thread stopped.");
        };
    }

}
