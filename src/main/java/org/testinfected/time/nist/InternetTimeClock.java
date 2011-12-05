package org.testinfected.time.nist;

import org.testinfected.time.Clock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Date;

public class InternetTimeClock implements Clock {
    private final String host;
    private final int port;
    private TimeServerDialect timeServerDialect = new NISTDialect();

    public InternetTimeClock(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void speak(TimeServerDialect timeServerDialect) {
        this.timeServerDialect = timeServerDialect;
    }

    public Date now() {
        try {
            String now = obtainTimeCodeFromServer();
            return timeServerDialect.translate(now);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String obtainTimeCodeFromServer() throws IOException {
        Socket socket = null;
        try {
            socket = new Socket(host, port);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            skip(nextLine(reader));
            return nextLine(reader);
        } finally {
            close(socket);
        }
    }

    private String nextLine(BufferedReader reader) throws IOException {
        return reader.readLine();
    }

    @SuppressWarnings({"UnusedParameters"})
    private void skip(String line) throws IOException {
    }

    private void close(Socket socket) throws IOException {
        if (socket != null) socket.close();
    }
}
