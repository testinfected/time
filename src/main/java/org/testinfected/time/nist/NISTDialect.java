package org.testinfected.time.nist;

import org.testinfected.time.DaytimeDialect;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Time code format is:
 * <pre>JJJJJ YY-MM-DD HH:MM:SS TT L H msADV UTC(NIST) OTM</pre>
 * (see http://www.nist.gov/pml/div688/grp40/its.cfm).
 *
 * The relevant part for us is <pre>YY-MM-DD HH:MM:SS</pre>
 **/
public class NISTDialect implements DaytimeDialect {
    public static final DaytimeDialect INSTANCE = new NISTDialect();

    private static final String DATE_FORMAT = "yy-MM-dd";
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final String TIME_CODE_FORMAT = "JJJJJ %s %s TT L H msADV UTC(NIST) *";

    private static DateFormat usingUTC(String pattern) {
        DateFormat format = new SimpleDateFormat(pattern);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format;
    }

    private final DateFormat parser = usingUTC(DATE_FORMAT + " " + TIME_FORMAT);

    private NISTDialect() {}

    public Date decode(String timeCode) throws ParseException {
        return parseTimeCode(timeCode);
    }

    public String encode(Date pointInTime) {
        return formatTimeCode(pointInTime);
    }

    private Date parseTimeCode(String timeCode) throws ParseException {
        return parser.parse(dateTimeFrom(timeCode));
    }

    private String dateTimeFrom(String timeCode) {
        return timeCode.substring(6, 23);
    }

    private String formatTimeCode(Date pointInTime) {
        return String.format(TIME_CODE_FORMAT, formatDate(pointInTime), formatTime(pointInTime));
    }

    private String formatTime(Date pointInTime) {
        return usingUTC(TIME_FORMAT).format(pointInTime);
    }

    private String formatDate(Date pointInTime) {
        return usingUTC(DATE_FORMAT).format(pointInTime);
    }
}
