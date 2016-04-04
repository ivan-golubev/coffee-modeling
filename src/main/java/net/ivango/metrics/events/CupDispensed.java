package net.ivango.metrics.events;

import net.ivango.entities.CoffeeType;

/**
 * Event used to track the dispensed cups.
 *
 * Created by Ivan Golubev <igolubev@ea.com> on 4/3/16.
 */
public class CupDispensed implements Event {

    private CoffeeType coffeeType;
    private int coffeeMachineNumber;

    public CupDispensed(CoffeeType coffeeType, int coffeeMachineNumber) {
        this.coffeeType = coffeeType;
        this.coffeeMachineNumber = coffeeMachineNumber;
    }

    public CoffeeType getCoffeeType() { return coffeeType; }

    public int getCoffeeMachineNumber() { return coffeeMachineNumber; }
}
