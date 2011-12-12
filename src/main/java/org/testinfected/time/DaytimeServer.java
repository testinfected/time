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

/**
 * Time code format is
 * JJJJJ YR-MO-DA HH:MM:SS TT L H msADV UTC(NIST) OTM
 * see http://www.nist.gov/pml/div688/grp40/its.cfm
 **/
public class DaytimeServer {
    private static final String LINE_FEED = "\n";

    private final int port;
    private final Executor executor = Executors.newSingleThreadExecutor();

    private ServerSocket server;
    private Clock internalClock = new SystemClock();
    private DaytimeDialect dialect = NISTDialect.INSTANCE;

    public static DaytimeServer listeningOnPort(int port) {
        return new DaytimeServer(port);
    }

    private DaytimeServer(int port) {
        this.port = port;
    }

    public void setInternalClock(Clock clock) {
        this.internalClock = clock;
    }

    public void speak(DaytimeDialect dialect) {
        this.dialect = dialect;
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
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    private boolean shouldContinue() {
        return !serverClosed();
    }

    private boolean serverClosed() {
        return server.isClosed();
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        System.out.println("Listening on port " + port + "...");
        final DaytimeServer server = DaytimeServer.listeningOnPort(port);
        closeServerOnShutdown(server);
        server.start();
    }

    private static void closeServerOnShutdown(final DaytimeServer server) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    System.out.println("\nShutting down...");
                    server.stop();
                    System.out.println("Done.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
