package org.kendar.servers;

public interface AnsweringServer extends Runnable {
    void run();

    boolean shouldRun();
}
