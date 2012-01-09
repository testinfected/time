package org.testinfected.time;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
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
import java.net.InetAddress;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.testinfected.time.lib.DateBuilder.aDate;

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
    String localhost = "127.0.0.1";

    Exception serverError;
    ServerMonitor captureError = new ServerMonitor() {
        public void exceptionOccurred(Exception e) {
            serverError = e;
        }
        public void clientConnected(InetAddress clientAddress) {}
    };
    ServerMonitor monitor = context.mock(ServerMonitor.class);

    @Before public void
    startServer() throws IOException {
        context.checking(new Expectations() {{
            allowing(serverDialect).encode(currentTime); will(returnValue(timeCode));
        }});
        server.setInternalClock(BrokenClock.stoppedAt(currentTime));
        server.addMonitor(captureError);
        server.start();
    }

    @After public void
    stopServer() throws Exception {
        allowExceptionOnStop(monitor);
        server.stop();
        executor.shutdown();
    }

    @Test public void
    providesTimeCodeBasedOnInternalClock() throws Exception {
        TaskList<String> pending = fetchTimeCode();
        String timeCode = pending.getSingleResult(timeout);

        assertNoServerError();
        assertThat("time code", timeCode, equalTo(timeCode));
    }

    @Test public void
    supportsMultipleConcurrentClients() throws Exception {
        TaskList<String> pending = fetchTimeCode(clientCount);
        int fulfilled = pending.await(timeout);

        assertNoServerError();
        assertThat("requests fulfilled", fulfilled, equalTo(clientCount));
    }

    @Test public void
    notifiesWhenClientsConnect() throws Exception {
        context.checking(new Expectations() {{
            oneOf(monitor).clientConnected(with(host(localhost)));
        }});
        server.addMonitor(monitor);
        fetchTimeCode().await(timeout);
        assertNoServerError();
    }

    private void allowExceptionOnStop(ServerMonitor monitor) {
        server.removeMonitor(monitor);
    }

    private TaskList<String> fetchTimeCode() throws InterruptedException {
        return fetchTimeCode(1);
    }

    private TaskList<String> fetchTimeCode(int clientCount) throws InterruptedException {
        return executor.spawn(new TimeRequest(serverHost, serverPort), clientCount);
    }

    private void assertNoServerError() throws Exception {
        if (serverError != null) throw serverError;
    }

    private Matcher<InetAddress> host(String  host) {
        return new FeatureMatcher<InetAddress, String>(equalTo(host), "an host", "host") {
            protected String featureValueOf(InetAddress actual) {
                return actual.getHostAddress();
            }
        };
    }
}
