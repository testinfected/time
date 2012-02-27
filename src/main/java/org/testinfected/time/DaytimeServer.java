package org.testinfected.time;

import org.testinfected.time.lib.Announcer;
import org.testinfected.time.lib.SystemClock;
import org.testinfected.time.nist.NISTDialect;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DaytimeServer {
    private static final String LINE_FEED = "\n";

    private final int port;
    private final DaytimeDialect dialect;
    private final Executor executor = Executors.newCachedThreadPool();
    private final Announcer<ServerMonitor> monitors = Announcer.to(ServerMonitor.class);

    private ServerSocket server;
    private Clock internalClock = new SystemClock();

    public DaytimeServer(int port) {
        this(port, NISTDialect.INSTANCE);
    }

    public DaytimeServer(int port, DaytimeDialect dialect) {
        this.port = port;
        this.dialect = dialect;
    }

    public void setInternalClock(Clock clock) {
        this.internalClock = clock;
    }

    public void addMonitor(ServerMonitor monitor) {
        monitors.subscribe(monitor);
    }

    public void removeMonitor(ServerMonitor monitor) {
        monitors.unsubscribe(monitor);
    }

    public void start() throws IOException {
        startServer();
        startResponding();
    }

    private void startServer() throws IOException {
        server = new ServerSocket(port);
    }

    private void startResponding() {
        executor.execute(new Runnable() {
            public void run() {
                serveClients();
            }
        });
    }

    public void stop() throws IOException {
        server.close();
    }

    private void reportException(Exception e) {
        monitors.announce().exceptionOccurred(e);
    }

    private void serveClients() {
        while (shouldContinue()) {
            try {
                serve(server.accept());
            } catch (IOException e) {
                reportException(e);
            }
        }
    }

    private void serve(final Socket client) {
        executor.execute(new Runnable() {
            public void run() {
                respondTo(client);
            }
        });
    }

    private void respondTo(Socket client) {
        try {
            reportClientConnected(client);
            Writer writer = new OutputStreamWriter(client.getOutputStream());
            writer.write(LINE_FEED);
            writer.write(timeCode());
            writer.write(LINE_FEED);
            writer.flush();
            reportTimeGiven(timeCode());
        } catch (IOException e) {
            reportException(e);
        } finally {
            closeSocket(client);
        }
    }

    private void reportTimeGiven(String timeCode) {
        monitors.announce().timeGiven(timeCode);
    }

    private void reportClientConnected(Socket client) {
        monitors.announce().clientConnected(client.getInetAddress());
    }

    private String timeCode() {
        return dialect.encode(internalClock.now());
    }

    private void closeSocket(Socket socket) {
        try { socket.close(); } catch (IOException ignored) {}
    }

    private boolean shouldContinue() {
        return !server.isClosed();
    }
}
