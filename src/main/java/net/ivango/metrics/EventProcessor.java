package net.ivango.metrics;

import com.github.rjeschke.txtmark.Processor;
import net.ivango.CoffeeModeling;
import net.ivango.entities.CoffeeType;
import net.ivango.entities.PaymentType;
import net.ivango.metrics.events.CupDispensed;
import net.ivango.metrics.events.CupSold;
import net.ivango.metrics.events.CustomerServised;
import net.ivango.metrics.events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Ivan Golubev <igolubev@ea.com> on 4/3/16.
 */
public class EventProcessor {

    private Collection<CupSold> cupSoldEvents = new LinkedBlockingQueue<>();
    private Collection<CupDispensed> cupDispensedEvents = new LinkedBlockingQueue<>();
    private Collection<CustomerServised> customerServicedEvents = new LinkedBlockingQueue<>();

    private final static String reportPath = "target/report-%s-programmers.html";

    private Logger logger = LoggerFactory.getLogger(EventProcessor.class);

    public void submitEvent(Event event) {
        if (event instanceof CupSold) { cupSoldEvents.add((CupSold) event ); }
        else if (event instanceof  CupDispensed) { cupDispensedEvents.add( (CupDispensed) event); }
        else if (event instanceof CustomerServised) { customerServicedEvents.add( (CustomerServised) event ); }
    }

    private void cleanBlockingQueues() {
        cupSoldEvents = new ArrayList<>( cupSoldEvents );
        cupDispensedEvents = new ArrayList<>( cupDispensedEvents );
        customerServicedEvents = new ArrayList<>( customerServicedEvents );
    }

    public void processEvents() {
        /* thread-safe collection are no longer needed */
        cleanBlockingQueues();

        String cupsSoldStats = Processor.process(getCupsSoldStats());
        String coffeeMachineStats = Processor.process(getCoffeeMachineStats());
        String customerWaitStats = Processor.process(getCustomerWaitStats());

        Path outputPath = new File(String.format(reportPath, cupSoldEvents.size())).toPath();
        logger.info("Generating report: " + outputPath);
        try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write(cupsSoldStats);
            writer.write(coffeeMachineStats);
            writer.write(customerWaitStats);
        } catch (Exception e) {
            logger.error("Error during report generation: ", e);
        }
    }

    private String getCoffeeMachineStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Coffee machine stats").append("\n");

        for (int i=0; i < CoffeeModeling.PICK_COFFEE_PARALLELISM; i++) {
            final int machineNumber = i + 1;
            long totalCupsDispensed = cupDispensedEvents.stream().filter( c -> c.getCoffeeMachineNumber() == machineNumber ).count();
            long espressoCupsDispensed = cupDispensedEvents.stream().filter(c -> (c.getCoffeeMachineNumber() == machineNumber && c.getCoffeeType() == CoffeeType.ESPRESSO) ).count();
            long latteCupsDispensed = cupDispensedEvents.stream().filter(c -> (c.getCoffeeMachineNumber() == machineNumber && c.getCoffeeType() == CoffeeType.LATTE)).count();
            long macchiatoCupsDispensed = cupDispensedEvents.stream().filter(c -> (c.getCoffeeMachineNumber() == machineNumber && c.getCoffeeType() == CoffeeType.MACCHIATO) ).count();
            long cappuchinoCupsDispensed = cupDispensedEvents.stream().filter(c -> (c.getCoffeeMachineNumber() == machineNumber && c.getCoffeeType() == CoffeeType.CAPPUCCINO)).count();

            sb.append("##Coffee machine ").append(machineNumber).append("\n\n")
                    .append("Total cups dispensed: ").append(totalCupsDispensed).append("\n\n")
                    .append("Espresso dispensed: ").append(espressoCupsDispensed).append("\n\n")
                    .append("Latte dispensed: ").append(latteCupsDispensed).append("\n\n")
                    .append("Macchiato dispensed: ").append(macchiatoCupsDispensed).append("\n\n")
                    .append("Cappuchino dispensed: ").append(cappuchinoCupsDispensed).append("\n\n\n\n");
        }
        return sb.toString();
    }

    private String getCustomerWaitStats() {
        long minServiceTime = customerServicedEvents.stream().min((p1, p2) -> Long.compare( p1.getServiceTime(), p2.getServiceTime())).get().getServiceTime();
        long maxServiceTime = customerServicedEvents.stream().max((p1, p2) -> Long.compare(p1.getServiceTime(), p2.getServiceTime())).get().getServiceTime();
        double avgServiceTime = customerServicedEvents.stream().mapToLong( CustomerServised::getServiceTime ).average().getAsDouble();

        StringBuilder sb = new StringBuilder();
        sb.append("# Service time").append("\n")
                .append("Min service time: ").append(minServiceTime).append(" milliseconds").append("\n\n")
                .append("Average service time: ").append(avgServiceTime).append(" milliseconds").append("\n\n")
                .append("Max service time: ").append(maxServiceTime).append(" milliseconds").append("\n\n");
        return sb.toString();
    }

    private String getCupsSoldStats() {
        int totalCupsSold = cupSoldEvents.size();
        long cupsSoldForCash = cupSoldEvents.stream().filter( c -> c.getPaymentType() == PaymentType.CASH).count();
        long cupsSoldForCredit = cupSoldEvents.stream().filter( c -> c.getPaymentType() == PaymentType.CREDIT).count();

        StringBuilder sb = new StringBuilder();
        sb.append("# Cups Sold").append("\n")
                .append("Total: ").append(totalCupsSold).append("\n\n")
                .append("Cups sold for cash: ").append(cupsSoldForCash).append("\n\n")
                .append("Cups sold for credit: ").append(cupsSoldForCredit).append("\n\n");
        return sb.toString();
    }

}
