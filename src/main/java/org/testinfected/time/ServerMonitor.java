package org.testinfected.time;

import java.net.InetAddress;

public interface ServerMonitor {
    void clientConnected(InetAddress clientAddress);

    void timeGiven(String timeCode);

    void exceptionOccurred(Exception e);
}
