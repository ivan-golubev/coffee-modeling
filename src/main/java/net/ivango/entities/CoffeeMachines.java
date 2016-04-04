package net.ivango.entities;

import com.google.common.collect.ImmutableMap;
import net.ivango.metrics.EventProcessor;
import net.ivango.metrics.events.CupDispensed;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ivan Golubev <igolubev@ea.com> on 4/3/16.
 */
public class CoffeeMachines {

    private EventProcessor eventProcessor;

    private Map<CoffeeType, Integer> coffeeToDelayMap = ImmutableMap.<CoffeeType, Integer>builder()
            .put(CoffeeType.ESPRESSO, Timeouts.POUR_ESPRESSO_TIMEOUT)
            .put(CoffeeType.LATTE, Timeouts.POUR_LATTE_TIMEOUT)
            .put(CoffeeType.MACCHIATO, Timeouts.POUR_MACCIATTO_TIMEOUT)
            .put(CoffeeType.CAPPUCCINO, Timeouts.POUR_CAPPUCHINO_TIMEOUT)
            .build();

    public CoffeeMachines(EventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    public Cup pourCoffee(Cup cup, CoffeeType coffeeType) throws InterruptedException {
        /* put the cup under the outlet */
        TimeUnit.MILLISECONDS.sleep(Timeouts.PUT_UNDER_THE_OUTLET_TIMEOUT);
        /* wait till the cup is filled */
        TimeUnit.MILLISECONDS.sleep(coffeeToDelayMap.get(coffeeType));

        eventProcessor.submitEvent( new CupDispensed(coffeeType) );
        return cup;
    }

    public CoffeeType[] getCoffeeTypes() { return CoffeeType.values(); }
}
