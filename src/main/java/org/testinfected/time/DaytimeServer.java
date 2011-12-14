package org.testinfected.time;

import org.testinfected.time.nist.NISTDialect;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.lang.String.format;

public class DaytimeServer {
    private static final String LINE_FEED = "\n";

    private final int port;
    private final DaytimeDialect dialect;
    private final Executor executor = Executors.newSingleThreadExecutor();

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

    // TODO plug-in monitor to externalize how exception are handled
    private void exceptionOccurred(Exception e) {
    }

    private void serveClients() {
        while (shouldContinue()) {
            try {
                respondTo(server.accept());
            } catch (IOException e) {
                exceptionOccurred(e);
            }
        }
    }

    private void respondTo(Socket client) {
        try {
            Writer writer = new OutputStreamWriter(client.getOutputStream());
            writer.write(LINE_FEED);
            writer.write(timeCode());
            writer.write(LINE_FEED);
            writer.flush();
        } catch (IOException e) {
            exceptionOccurred(e);
        } finally {
            closeSocket(client);
        }
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
