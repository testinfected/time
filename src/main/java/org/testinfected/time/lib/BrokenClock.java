package org.testinfected.time.lib;

import org.testinfected.time.Clock;

import java.util.Date;

public class BrokenClock implements Clock {

    public static Clock stoppedAt(Date pointInTime) {
        return new BrokenClock(pointInTime);
    }

    private final Date pointInTime;

    private BrokenClock(Date pointInTime) {
        this.pointInTime = pointInTime;
    }

    public Date now() {
        return pointInTime;
    }
}
