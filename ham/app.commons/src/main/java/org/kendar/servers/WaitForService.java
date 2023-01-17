package org.kendar.servers;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface WaitForService {
    void waitForService(String name);
    void waitForService(String name, Runnable runnable);
}
