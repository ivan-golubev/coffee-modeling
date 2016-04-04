package net.ivango.metrics.events;

import net.ivango.entities.CoffeeType;

/**
 * Created by Ivan Golubev <igolubev@ea.com> on 4/3/16.
 */
public class CupDispensed implements Event {

    private CoffeeType coffeeType;
    public CupDispensed(CoffeeType coffeeType) { this.coffeeType = coffeeType; }

    public CoffeeType getCoffeeType() { return coffeeType; }
}
