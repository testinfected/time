package org.testinfected.time;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.Callable;

public class TimeRequest implements Callable<String> {

    private final String serverHost;
    private final int serverPort;

    public TimeRequest(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public String call() throws Exception {
        Socket socket = null;
        try {
            socket = new Socket(serverHost, serverPort);
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
