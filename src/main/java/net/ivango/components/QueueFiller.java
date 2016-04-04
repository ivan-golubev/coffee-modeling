package net.ivango.components;

import net.ivango.entities.PaymentType;
import net.ivango.entities.Programmer;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simulates system load: used to fill the income queue with customers (programmers).
 *
 * Created by Ivan Golubev <igolubev@ea.com> on 4/3/16.
 */
public class QueueFiller {

    private Random random = new Random();

    /**
     * Generates a customer with random properties:
     * payment type.
     * */
    private Programmer randomCustomer(){
        return new Programmer(
                PaymentType.values()[random.nextInt(PaymentType.values().length)]
        );
    }

    /**
     * Generate a list of random customers to use for simulation.
     * @param amount – size of a list to generate.
     * */
    public List<Programmer> generateRandomCustomers(int amount) {
        return Stream.generate(this::randomCustomer).limit(amount).collect(Collectors.toList());
    }
}
