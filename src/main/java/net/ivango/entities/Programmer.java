package net.ivango.entities;

import net.ivango.config.Config;
import org.joda.time.DateTime;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static net.ivango.config.Properties.*;

/**
 * Created by Ivan Golubev <igolubev@ea.com> on 4/3/16.
 */
public class Programmer {

    private static Random RANDOM = new Random();

    private PaymentType paymentType;
    private DateTime serviceStart;
    private CoffeeType selectedCoffee;

    public Programmer(PaymentType paymentType) { this.paymentType = paymentType; }

    public PaymentType getPaymentType() { return paymentType; }
    public DateTime getServiceStart() { return serviceStart; }
    public CoffeeType getSelectedCoffee() { return selectedCoffee; }
    public void selectFavouriteCoffee(CoffeeType[] availableCoffeeTypes) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(Config.get(COFFEE_SELECTION_TIMEOUT));
        this.selectedCoffee = availableCoffeeTypes [
                Programmer.RANDOM.nextInt(availableCoffeeTypes.length)
        ];
    }

    public void markServiceStart() { this.serviceStart = DateTime.now();}

    public Cup findCup() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(Config.get(FIND_CUP_TIMEOUT));
        return new Cup();
    }

    public void takeTheCupAndLeave(Cup cup) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(Config.get(TAKE_CUP_AND_LEAVE_TIMEOUT));
    }
}
