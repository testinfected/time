package org.testinfected.time;

import java.text.ParseException;
import java.util.Date;

public interface DaytimeDialect {
    Date decode(String timeCode) throws ParseException;

    String encode(Date pointInTime);
}
