package org.testinfected.time.lib;

import org.testinfected.time.Clock;

import java.util.Date;

public class SystemClock implements Clock {

    public Date now() {
        return new Date();
    }
}
