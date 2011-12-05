package org.testinfected.time.nist;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testinfected.time.BrokenClock;

import java.io.IOException;
import java.util.Date;

import static org.testinfected.time.builder.DateBuilder.aDate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(JMock.class)
public class InternetTimeClockTest {

    Mockery context = new JUnit4Mockery();
    TimeServerDialect timeServerDialect = context.mock(TimeServerDialect.class);
    int serverPort = 10013;
    InternetTimeServer server = InternetTimeServer.listeningOnPort(serverPort);
    Date serverTime = aDate().build();

    InternetTimeClock clock = new InternetTimeClock("localhost", serverPort);

    @Before
    public void startTimeServer() throws IOException {
        server.setInternalClock(BrokenClock.clockStoppedAt(serverTime));
        server.start();
    }

    @After
    public void stopTimeServer() throws IOException {
        server.stop();
    }

    @Test public void
    obtainsCurrentTimeFromRemoteServer() throws Exception {
        context.checking(new Expectations() {{
            oneOf(timeServerDialect).translate(server.timeCode()); will(returnValue(serverTime));
        }});
        clock.speak(timeServerDialect);
        assertThat("internet time", clock.now(), equalTo(serverTime));
    }

}

