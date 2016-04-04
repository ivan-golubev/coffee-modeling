package net.ivango.metrics.events;

import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * * Event used to track the serviced customers.
 *
 * Created by Ivan Golubev <igolubev@ea.com> on 4/3/16.
 */
public class CustomerServiced implements Event {
    private long serviceTime;

    public CustomerServiced(DateTime start, DateTime end) {
        this.serviceTime = new Interval(start, end).toDurationMillis();
    }

    public long getServiceTime() { return serviceTime; }
}
