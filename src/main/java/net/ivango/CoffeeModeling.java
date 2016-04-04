package net.ivango;

import net.ivango.entities.CashRegister;
import net.ivango.entities.CoffeeMachines;
import net.ivango.entities.Cup;
import net.ivango.entities.Programmer;
import net.ivango.metrics.EventProcessor;
import net.ivango.metrics.events.CustomerServised;
import org.joda.time.DateTime;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Created by Ivan Golubev <igolubev@ea.com> on 4/3/16.
 */
public class CoffeeModeling {

    public static final int COFFEE_SELECT_PARALLELISM = 10, PAYMENT_PARALLELISM = 5, PICK_COFFEE_PARALLELISM = 2;

    private BlockingQueue<Programmer> paymentQueue = new LinkedBlockingDeque<>(), dispenseQueue = new LinkedBlockingDeque<>();

    private ExecutorService coffeeSelectionThreadPool = Executors.newFixedThreadPool(COFFEE_SELECT_PARALLELISM);
    private ExecutorService paymentThreadPool = Executors.newFixedThreadPool(PAYMENT_PARALLELISM);
    private ExecutorService pickCoffeeThreadPool = Executors.newFixedThreadPool(PICK_COFFEE_PARALLELISM);

    private CountDownLatch doneSignal;

    private EventProcessor eventProcessor = new EventProcessor();
    private CashRegister cashRegister = new CashRegister(eventProcessor);
    private CoffeeMachines coffeeMachines = new CoffeeMachines(eventProcessor);

    public CoffeeModeling() {
        /* initialize the worker threads */
        for (int i=0; i < PAYMENT_PARALLELISM; i++) {
            paymentThreadPool.submit(paymentTask());
        }
        for (int i=0; i< PICK_COFFEE_PARALLELISM; i++) {
            pickCoffeeThreadPool.submit(pickCoffeeTask());
        }
    }

    private void shutdown(){
        paymentThreadPool.shutdown();
        pickCoffeeThreadPool.shutdown();
    }

    private Callable<Void> selectCoffeeTask(Programmer programmer) {
        return () -> {
            programmer.markServiceStart();
            System.out.println("Selecting coffee...");
            programmer.selectFavouriteCoffee( coffeeMachines.getCoffeeTypes() );
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
                    System.out.println("Paying...");
                    cashRegister.pay( programmer.getPaymentType() );

                    /* send this guy to the dispense queue */
                    dispenseQueue.offer( programmer );
                }
            } catch (InterruptedException ie) {
                System.out.println("Payment worker thread interrupted.");
            }
            System.out.println("Payment worker thread stopped.");
        };
    }

    private Runnable pickCoffeeTask() {
        return () -> {
            try {
                while ( !Thread.interrupted() ) {

                    /* take next guy from the dispense queue */
                    Programmer programmer = dispenseQueue.take();

                    /* 1. First - the programmer looks for a cup */
                    Cup cup = programmer.findCup();

                    /* 2. Then he puts it under the outlet and waits till the cup is filled */
                    cup = coffeeMachines.pourCoffee( cup, programmer.getSelectedCoffee() );

                    /* 3. Finally he takes the cup and leaves */
                    programmer.takeTheCupAndLeave( cup );

                    /* mark the completion of this task */
                    doneSignal.countDown();

                    eventProcessor.submitEvent(new CustomerServised(programmer.getServiceStart(), DateTime.now()));
                }
            } catch (InterruptedException ie) {
                System.out.println("Pick Coffee worker thread interrupted.");
            }
            System.out.println("Pick Coffee worker thread stopped.");
        };
    }

    public void launch(List<Programmer> arrivedCustomers) {
        try {
            doneSignal = new CountDownLatch(arrivedCustomers.size());
            /* 1. First all the programmers select their favourite coffee */
            coffeeSelectionThreadPool.invokeAll(
                    arrivedCustomers.stream().map(this::selectCoffeeTask).collect(Collectors.toList())
            );
            coffeeSelectionThreadPool.shutdown();
            /* 2. Then they pay for it using cash or a credit card – done via worker threads plus blocking queue */
            /* 3. Then they pick the coffee and leave - done via worker threads plus blocking queue */

            /* Once all the work is complete - gather the stats */
            doneSignal.await();
            eventProcessor.processEvents();
        } catch (Exception e) {}
        finally {
            shutdown();
        }
    }
}
