package net.ivango.entities;

import java.util.Date;

/**
 * Created by Ivan Golubev <igolubev@ea.com> on 4/3/16.
 */
public class Programmer {
    private PaymentType paymentType;
    private Date serviceStart;

    public Programmer(PaymentType paymentType) { this.paymentType = paymentType; }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public Date getServiceStart() {
        return serviceStart;
    }

    public void setServiceStart(Date serviceStart) {
        this.serviceStart = serviceStart;
    }
}
