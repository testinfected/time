package org.testinfected.time;

import org.testinfected.time.nist.NISTDialect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Date;

public class DaytimeClient implements Clock {
    private final String serverHost;
    private final int serverPort;
    private final DaytimeDialect serverDialect;

    public DaytimeClient(String serverHost, int serverPort, DaytimeDialect serverDialect) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.serverDialect = serverDialect;
    }

    public Date now() {
        try {
            return serverDialect.decode(fetchTimeCode());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String fetchTimeCode() throws IOException {
        Socket connection = null;
        try {
            connection = connectToServer();
            BufferedReader input = bufferInput(connection);
            skip(nextLine(input));
            return nextLine(input);
        } finally {
            close(connection);
        }
    }

    private Socket connectToServer() throws IOException {
        return new Socket(serverHost, serverPort);
    }

    private BufferedReader bufferInput(Socket connection) throws IOException {
        return new BufferedReader(new InputStreamReader(connection.getInputStream()));
    }

    private String nextLine(BufferedReader reader) throws IOException {
        return reader.readLine();
    }

    @SuppressWarnings({"UnusedParameters"})
    private void skip(String line) throws IOException {
    }

    private void close(Socket connection) throws IOException {
        if (connection != null) connection.close();
    }

    public static void main(String[] args) throws Exception {
        System.out.println(String.format("Connecting to daytime server at %s on port %d", host(args), port(args)));
        DaytimeClient clock = new DaytimeClient(host(args), port(args), NISTDialect.INSTANCE);
        System.out.println("Current time is " + clock.now());
    }

    private static int port(String[] args) {
        return Integer.parseInt(args[1]);
    }

    private static String host(String[] args) {
        return args[0];
    }
                                  }
