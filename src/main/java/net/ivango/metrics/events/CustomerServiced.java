package net.ivango.metrics.events;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Event used to track the serviced customers.
 *
 * Created by Ivan Golubev <igolubev@ea.com> on 4/3/16.
 */
public class CustomerServiced implements Event {
    /* service time in milliseconds */
    private long serviceTime;

    public CustomerServiced(LocalDateTime start, LocalDateTime end) {
        this.serviceTime = ChronoUnit.MILLIS.between(start, end);
    }

    public long getServiceTime() { return serviceTime; }
}
