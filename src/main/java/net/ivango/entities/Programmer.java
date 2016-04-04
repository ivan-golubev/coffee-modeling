package net.ivango.entities;

import net.ivango.config.Config;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static net.ivango.config.Properties.*;

/**
 * Simulates a customer.
 *
 * Created by Ivan Golubev <igolubev@ea.com> on 4/3/16.
 */
public class Programmer {

    private PaymentType paymentType;
    private LocalDateTime serviceStart;
    private CoffeeType selectedCoffee;

    private static Random RANDOM = new Random();

    public Programmer(PaymentType paymentType) { this.paymentType = paymentType; }

    public PaymentType getPaymentType() { return paymentType; }
    public LocalDateTime getServiceStart() { return serviceStart; }
    public CoffeeType getSelectedCoffee() { return selectedCoffee; }

    /**
     * Simulates the coffee selection process.
     * */
    public void selectFavouriteCoffee(CoffeeType[] availableCoffeeTypes) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(Config.get(COFFEE_SELECTION_TIMEOUT));
        this.selectedCoffee = availableCoffeeTypes [
                Programmer.RANDOM.nextInt(availableCoffeeTypes.length)
        ];
    }

    /**
     * Tracks the service start. Used for reporting.
     * */
    public void markServiceStart() { this.serviceStart = LocalDateTime.now();}

    /**
     * Simulates the coffee searching process.
     * */
    public Cup findCup() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(Config.get(FIND_CUP_TIMEOUT));
        return new Cup();
    }

    /**
     * Simulates the leaving process.
     * */
    public void takeTheCupAndLeave(Cup cup) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(Config.get(TAKE_CUP_AND_LEAVE_TIMEOUT));
    }
}
