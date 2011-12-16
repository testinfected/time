package org.testinfected.time;

import java.net.InetAddress;

public interface ServerMonitor {
    void clientConnected(InetAddress clientAddress);

    void exceptionOccurred(Exception e);
}
