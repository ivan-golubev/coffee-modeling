package net.ivango.metrics;

import net.ivango.CoffeeModeling;
import net.ivango.entities.CoffeeType;
import net.ivango.entities.PaymentType;
import net.ivango.metrics.events.CupDispensed;
import net.ivango.metrics.events.CupSold;
import net.ivango.metrics.events.CustomerServised;
import net.ivango.metrics.events.Event;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Ivan Golubev <igolubev@ea.com> on 4/3/16.
 */
public class EventProcessor {

    private Queue<CupSold> cupSoldEvents = new LinkedBlockingQueue<>();
    private Queue<CupDispensed> cupDispensedEvents = new LinkedBlockingQueue<>();
    private Queue<CustomerServised> customerServicedEvents = new LinkedBlockingQueue<>();

    public void submitEvent(Event event) {
        if (event instanceof CupSold) { cupSoldEvents.add( (CupSold) event ); }
        else if (event instanceof  CupDispensed) { cupDispensedEvents.add( (CupDispensed) event); }
        else if (event instanceof CustomerServised) { customerServicedEvents.add( (CustomerServised) event ); }
    }

    public void processEvents() {
        int totalCupsSold = cupSoldEvents.size();
        long cupsSoldForCash = cupSoldEvents.stream().filter( c -> c.getPaymentType() == PaymentType.CASH).count();
        long cupsSoldForCredit = cupSoldEvents.stream().filter( c -> c.getPaymentType() == PaymentType.CREDIT).count();

        for (int i=0; i < CoffeeModeling.PICK_COFFEE_PARALLELISM; i++) {
            final int machineNumber = i + 1;
            long totalCupsDispensed = cupDispensedEvents.stream().filter( c -> c.getCoffeeMachineNumber() == machineNumber ).count();
            long espressoCupsDispensed = cupDispensedEvents.stream().filter(c -> (c.getCoffeeMachineNumber() == machineNumber && c.getCoffeeType() == CoffeeType.ESPRESSO) ).count();
            long latteCupsDispensed = cupDispensedEvents.stream().filter(c -> (c.getCoffeeMachineNumber() == machineNumber && c.getCoffeeType() == CoffeeType.LATTE) ).count();
            long macchiatoCupsDispensed = cupDispensedEvents.stream().filter(c -> (c.getCoffeeMachineNumber() == machineNumber && c.getCoffeeType() == CoffeeType.MACCHIATO) ).count();
            long cappuchinoCupsDispensed = cupDispensedEvents.stream().filter(c -> (c.getCoffeeMachineNumber() == machineNumber && c.getCoffeeType() == CoffeeType.CAPPUCCINO) ).count();
        }

        long minServiceTime = customerServicedEvents.stream().min((p1, p2) -> Long.compare( p1.getServiceTime(), p2.getServiceTime())).get().getServiceTime();
        long maxServiceTime = customerServicedEvents.stream().max((p1, p2) -> Long.compare(p1.getServiceTime(), p2.getServiceTime())).get().getServiceTime();
        double avgServiceTime = customerServicedEvents.stream().mapToLong( CustomerServised::getServiceTime ).average().getAsDouble();
    }
}
