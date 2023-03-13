package org.kendar.servers;

public interface WaitForService {
    void waitForService(String name);

    void waitForService(String name, Runnable runnable);
}
