package org.testinfected.time;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testinfected.time.lib.BrokenClock;

import java.io.IOException;
import java.util.Date;

import static org.testinfected.time.lib.DateBuilder.aDate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(JMock.class)
public class DaytimeClientTest {

    Mockery context = new JUnit4Mockery();
    DaytimeDialect serverDialect = context.mock(DaytimeDialect.class);

    String serverHost = "localhost";
    int serverPort = 10013;
    DaytimeServer server = new DaytimeServer(serverPort, serverDialect);
    Date currentTime = aDate().build();
    String timeCode = "current time";

    DaytimeClient clock = new DaytimeClient(serverHost, serverPort, serverDialect);

    @Before
    public void startTimeServer() throws IOException {
        context.checking(new Expectations() {{
            allowing(serverDialect).encode(currentTime); will(returnValue(timeCode));
        }});
        server.setInternalClock(BrokenClock.stoppedAt(currentTime));
        server.start();
    }

    @After
    public void stopTimeServer() throws IOException {
        server.stop();
    }

    @Test public void
    obtainsAndDecodesCurrentTimeFromRemoteServer() throws Exception {
        context.checking(new Expectations() {{
            oneOf(serverDialect).decode(timeCode); will(returnValue(currentTime));
        }});
        Date now = clock.now();
        assertThat("current time", now, equalTo(currentTime));
    }

}

