package net.ivango;

import net.ivango.entities.PaymentType;
import net.ivango.entities.Programmer;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Ivan Golubev <igolubev@ea.com> on 4/3/16.
 */
public class QueueFiller {

    private Random random = new Random();

    private Programmer randomCustomer(){
        return new Programmer(
                PaymentType.values()[random.nextInt(PaymentType.values().length)]
        );
    }

    public List<Programmer> generateRandomCustomers(int amount) {
        return Stream.generate(this::randomCustomer).limit(amount).collect(Collectors.toList());
    }
}
