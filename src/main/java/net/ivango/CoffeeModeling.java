package net.ivango;

import net.ivango.components.CashRegister;
import net.ivango.components.CoffeeMachine;
import net.ivango.config.Config;
import net.ivango.entities.Cup;
import net.ivango.entities.Programmer;
import net.ivango.metrics.EventProcessor;
import net.ivango.metrics.events.CustomerServised;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static net.ivango.config.Properties.*;

/**
 * Created by Ivan Golubev <igolubev@ea.com> on 4/3/16.
 */
public class CoffeeModeling {

    private ExecutorService coffeeSelectionThreadPool, paymentThreadPool, pickCoffeeThreadPool;
    private CountDownLatch doneSignal;
    private BlockingQueue<Programmer> paymentQueue = new LinkedBlockingDeque<>(),
                                      dispenseQueue = new LinkedBlockingDeque<>();

    private EventProcessor eventProcessor;
    private CashRegister cashRegister;
    private CoffeeMachine[] coffeeMachines;

    private Logger logger = LoggerFactory.getLogger(CoffeeModeling.class);

    public CoffeeModeling() {
        /* read the configuration */
        Config.init();

        /* initialize the components */
        cashRegister = new CashRegister(eventProcessor);
        eventProcessor = new EventProcessor();
        /* initialize the coffee machines */
        coffeeMachines = new CoffeeMachine[Config.get(PICK_COFFEE_PARALLELISM)];
        for (int i=0; i < Config.get(PICK_COFFEE_PARALLELISM); i++) { coffeeMachines[i] = new CoffeeMachine(eventProcessor, i+1); }

        /* initialize the executors */
        coffeeSelectionThreadPool = Executors.newFixedThreadPool(Config.get(COFFEE_SELECT_PARALLELISM));
        paymentThreadPool = Executors.newFixedThreadPool(Config.get(PAYMENT_PARALLELISM));
        pickCoffeeThreadPool = Executors.newFixedThreadPool(Config.get(PICK_COFFEE_PARALLELISM));

        /* initialize the worker threads */
        for (int i=0; i < Config.get(PAYMENT_PARALLELISM); i++) { paymentThreadPool.submit( paymentTask() ); }
        for (int i=0; i < Config.get(PICK_COFFEE_PARALLELISM); i++) { pickCoffeeThreadPool.submit( pickCoffeeTask() ); }
    }

    public void launch(List<Programmer> arrivedCustomers) {
        try {
            doneSignal = new CountDownLatch( arrivedCustomers.size() );
            /* 1. First all the programmers select their favourite coffee */
            coffeeSelectionThreadPool.invokeAll(
                    arrivedCustomers.stream().map(this::selectCoffeeTask).collect(Collectors.toList())
            );
            coffeeSelectionThreadPool.shutdown();
            /* 2. Then they pay for it using cash or a credit card – done via worker threads plus blocking queue */
            /* 3. Then they pick the coffee and leave - done via worker threads plus blocking queue */

            /* Once all the work is complete - gather the stats */
            doneSignal.await();
            paymentThreadPool.shutdown();
            pickCoffeeThreadPool.shutdown();

            eventProcessor.processEvents();
        } catch (Exception e) {
            logger.error("Error during execution: ", e);
        } finally {
            shutdown();
        }
    }

    /* Cleanup all the resources */
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

    private Callable<Void> selectCoffeeTask(Programmer programmer) {
        return () -> {
            programmer.markServiceStart();

            logger.debug("Selecting coffee...");
            programmer.selectFavouriteCoffee( CoffeeMachine.getCoffeeTypes() );
            /* send him to the payment queue */
            paymentQueue.offer(programmer);
            return null;
        };
    }

    private Runnable paymentTask() {
        return () -> {
            try {
                while ( !Thread.interrupted() ) {

                    /* take next guy from the payment queue */
                    Programmer programmer = paymentQueue.take();

                    /* the payment operation itself */
                    logger.debug("Paying...");
                    cashRegister.pay( programmer.getPaymentType() );

                    /* send this guy to the dispense queue */
                    dispenseQueue.offer( programmer );
                }
            } catch (InterruptedException ie) {
                logger.info("Payment worker thread interrupted.");
            }
            logger.info("Payment worker thread stopped.");
        };
    }

    private Random random = new Random();

    private Runnable pickCoffeeTask() {
        return () -> {
            try {
                while ( !Thread.interrupted() ) {

                    /* take next guy from the dispense queue */
                    Programmer programmer = dispenseQueue.take();

                    logger.debug("Picking coffee...");
                    /* 1. First - the programmer looks for a cup */
                    Cup cup = programmer.findCup();

                    /* 2. Then he puts it under the outlet and waits till the cup is filled */
                    CoffeeMachine coffeeMachine = coffeeMachines[random.nextInt(coffeeMachines.length)];
                    cup = coffeeMachine.pourCoffee( cup, programmer.getSelectedCoffee() );

                    logger.debug("Leaving...");
                    /* 3. Finally he takes the cup and leaves */
                    programmer.takeTheCupAndLeave( cup );

                    /* mark the completion of this task */
                    doneSignal.countDown();

                    eventProcessor.submitEvent(new CustomerServised(programmer.getServiceStart(), DateTime.now()));
                }
            } catch (InterruptedException ie) {
                logger.info("Pick Coffee worker thread interrupted.");
            }
            logger.info("Pick Coffee worker thread stopped.");
        };
    }

}
