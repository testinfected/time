package org.testinfected.time;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testinfected.time.DaytimeDialect;
import org.testinfected.time.DaytimeServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.*;

import static org.testinfected.time.BrokenClock.stoppedAt;
import static org.testinfected.time.builder.DateBuilder.aDate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(JMock.class)
public class DaytimeServerTest {

    Mockery context = new JUnit4Mockery();
    DaytimeDialect serverDialect = context.mock(DaytimeDialect.class);

    int TIMEOUT = 100;
    int serverPort = 10013;
    DaytimeServer server = new DaytimeServer(serverPort, serverDialect);
    Date currentTime = aDate().build();
    String timeCode = "current time";

    int clientCount = 25;
    ExecutorService clients = Executors.newCachedThreadPool();

    @Before
    public void
    startServer() throws IOException {
        context.checking(new Expectations() {{
            allowing(serverDialect).encode(currentTime); will(returnValue(timeCode));
        }});
        server.setInternalClock(BrokenClock.stoppedAt(currentTime));
        server.start();
    }

    @After
    public void
    stopServer() throws Exception {
        server.stop();
        clients.awaitTermination(250, TimeUnit.MILLISECONDS);
    }

    @Test
    public void
    providesCurrentTimeToClientBasedOnInternalClockTime() throws Exception {
        String response = waitFor(fetchTimeCode(), TIMEOUT);
        assertThat("time code", response, equalTo(timeCode));
    }

    private Future<String> fetchTimeCode() {
        return clients.submit(new TimeRequest());
    }

    private String waitFor(Future<String> timeCode, int timeout) throws Exception {
        return timeCode.get(timeout, TimeUnit.MILLISECONDS);
    }

    @Test
    public void
    supportsSeveralClientsAtTheSameTime() throws Exception {
        Collection<Future<String>> timeCodes = new ArrayList<Future<String>>();
        for (int i = 1; i <= clientCount; i++) {
            timeCodes.add(fetchTimeCode());
        }

        assertThat("clients served", clientsServed(timeCodes), equalTo(clientCount));
    }

    private int clientsServed(Collection<Future<String>> timeCodes) throws Exception {
        int requestsServed = 0;
        for (Future<String> timeCode : timeCodes) {
            try {
                waitFor(timeCode, TIMEOUT * clientCount);
                requestsServed++;
            } catch (TimeoutException skip) {
            }
        }
        return requestsServed;
    }

    private class TimeRequest implements Callable<String> {

        public String call() throws Exception {
            Socket socket = null;
            try {
                socket = new Socket("localhost", serverPort);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                nextLine(reader);
                return nextLine(reader);
            } finally {
                close(socket);
            }
        }

        private String nextLine(BufferedReader reader) throws IOException {
            return reader.readLine();
        }

        private void close(Socket socket) throws IOException {
            if (socket == null) return;
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
