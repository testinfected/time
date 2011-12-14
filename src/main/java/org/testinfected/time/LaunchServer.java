package org.testinfected.time;

import java.io.IOException;

public final class LaunchServer {

    public static void main(String[] args) throws Exception {
        int port = port(args);
        System.out.println("Listening on port " + port + "...");
        final DaytimeServer server = new DaytimeServer(port);
        stopOnShutdown(server);
        server.start();
    }

    private static int port(String... args) {
        return Integer.parseInt(args[0]);
    }

    private static void stopOnShutdown(final DaytimeServer server) {
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
