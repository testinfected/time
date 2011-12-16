package org.testinfected.time;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Date;

import static org.testinfected.time.builder.DateBuilder.aDate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(JMock.class)
public class DaytimeServerTest {

    Mockery context = new JUnit4Mockery();

    String serverHost = "localhost";
    int serverPort = 10013;
    DaytimeDialect serverDialect = context.mock(DaytimeDialect.class);
    DaytimeServer server = new DaytimeServer(serverPort, serverDialect);

    int clientCount = 100;
    int concurrentRequests = 25;
    int timeout = 100;
    ParallelExecutor executor = new ParallelExecutor(concurrentRequests);

    Date currentTime = aDate().build();
    String timeCode = "current time";

    @Before public void
    startServer() throws IOException {
        context.checking(new Expectations() {{
            allowing(serverDialect).encode(currentTime); will(returnValue(timeCode));
        }});
        server.setInternalClock(BrokenClock.stoppedAt(currentTime));
        server.start();
    }

    @After public void
    stopServer() throws Exception {
        server.stop();
        executor.shutdown();
    }

    @Test public void
    providesTimeCodeBasedOnInternalClock() throws Exception {
        Execution<String> pending = fetchTimeCode();
        assertThat("time code", pending.getResult(timeout), equalTo(timeCode));
    }

    @Test public void
    supportsMultipleConcurrentClients() throws Exception {
        Execution<String> pending = fetchTimeCode(clientCount);
        assertThat("requests fulfilled", pending.await(timeout), equalTo(clientCount));
    }

    private Execution<String> fetchTimeCode() throws InterruptedException {
        return fetchTimeCode(1);
    }

    private Execution<String> fetchTimeCode(int clientCount) throws InterruptedException {
        return executor.spawn(new TimeRequest(serverHost, serverPort), clientCount);
    }
}
