package net.ivango.metrics.events;

import net.ivango.entities.PaymentType;

/**
 * * Event used to track the sold cups.
 *
 * Created by Ivan Golubev <igolubev@ea.com> on 4/3/16.
 */
public class CupSold implements Event {

    private PaymentType paymentType;

    public CupSold(PaymentType paymentType) { this.paymentType = paymentType; }
    public PaymentType getPaymentType() { return paymentType; }
}
