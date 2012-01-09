package org.testinfected.time.nist;

import org.junit.Test;
import org.testinfected.time.DaytimeDialect;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.testinfected.time.lib.DateBuilder.aDate;

public class NISTDialectTest {

    DaytimeDialect dialect = NISTDialect.INSTANCE;

    String timeCode = "55488 10-10-19 16:03:15 20 0 0 448.0 UTC(NIST) *";
    TimeZone utc = TimeZone.getTimeZone("UTC");
    Date currentTime = aDate().onCalendar(2010, 10, 19).atTime(16, 3, 15).in(utc).build();

    @Test public void
    decodesTimeCodeToDateInUTC() throws ParseException {
        assertThat("time", dialect.decode(timeCode), equalTo(currentTime));
    }

    @Test public void
    encodesDateAsTimeCode() throws ParseException {
        assertThat("time code", dialect.encode(currentTime), equalTo("JJJJJ 10-10-19 16:03:15 TT L H msADV UTC(NIST) *"));
    }
}
