package net.ivango.components;

import com.google.common.collect.ImmutableMap;
import net.ivango.config.Config;
import net.ivango.entities.PaymentType;
import net.ivango.metrics.EventProcessor;
import net.ivango.metrics.events.CupSold;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static net.ivango.config.Properties.*;

/**
 * Created by Ivan Golubev <igolubev@ea.com> on 4/3/16.
 */
public class CashRegister {

    private EventProcessor eventProcessor;
    public CashRegister(EventProcessor eventProcessor) { this.eventProcessor = eventProcessor; }

    private Map<PaymentType, Integer> paymentToDelayMap = ImmutableMap.of(
            PaymentType.CASH, Config.get(CASH_PAYMENT_TIMEOUT),
            PaymentType.CREDIT, Config.get(CREDIT_PAYMENT_TIMEOUT)
    );

    public void pay(PaymentType paymentType) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep( paymentToDelayMap.get(paymentType) );
        eventProcessor.submitEvent(new CupSold(paymentType));
    }

}
