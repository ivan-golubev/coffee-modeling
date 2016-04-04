package net.ivango.metrics.events;

import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * Created by Ivan Golubev <igolubev@ea.com> on 4/3/16.
 */
public class CustomerServised implements Event {
    private long serviceTime;

    public CustomerServised(DateTime start, DateTime end) {
        this.serviceTime = new Interval(start, end).toDurationMillis();
    }

    public long getServiceTime() { return serviceTime; }
}
