package org.testinfected.time.nist;

import org.testinfected.time.Clock;
import org.testinfected.time.SystemClock;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.lang.String.format;

/**
 * Time code format is
 * JJJJJ YR-MO-DA HH:MM:SS TT L H msADV UTC(NIST) OTM
 * see http://www.nist.gov/pml/div688/grp40/its.cfm
 **/
public class InternetTimeServer {
    private final int port;
    private final Executor executor = Executors.newSingleThreadExecutor();

    private static final TimeZone utc = TimeZone.getTimeZone("UTC");
    private static final String DATE_FORMAT = "yy-MM-dd";
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final String TIME_CODE_FORMAT = "JJJJJ %s %s TT L H msADV UTC(NIST) *";
    private static final String LINE_FEED = "\n";
    
    private ServerSocket server;
    private Clock internalClock = new SystemClock();

    public static InternetTimeServer listeningOnPort(int port) {
        return new InternetTimeServer(port);
    }

    public InternetTimeServer(int port) {
        this.port = port;
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

    // TODO move time code logic to dialect
    public String timeCode() {
        return formatTimeCode(internalClock.now());
    }

    private static String formatTimeCode(Date pointInTime) {
        return format(TIME_CODE_FORMAT, date(pointInTime), time(pointInTime));
    }

    private static String time(Date pointInTime) {
        return using(TIME_FORMAT).format(pointInTime);
    }

    private static String date(Date pointInTime) {
        return using(DATE_FORMAT).format(pointInTime);
    }

    private static DateFormat using(String pattern) {
        DateFormat format = new SimpleDateFormat(pattern);
        format.setTimeZone(utc);
        return format;
    }

    private static void closeServerOnShutdown(final InternetTimeServer server) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    System.out.println("\nShutting down...");
                    server.stop();
                    System.out.println("Bye.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        System.out.println("Listening on port " + port + "...");
        final InternetTimeServer server = InternetTimeServer.listeningOnPort(port);
        closeServerOnShutdown(server);
        server.start();
    }
}
