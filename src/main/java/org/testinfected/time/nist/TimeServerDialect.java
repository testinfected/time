package org.testinfected.time.nist;

import java.text.ParseException;
import java.util.Date;

public interface TimeServerDialect {
    Date translate(String serverOutput) throws ParseException;
}
