package org.kendar.utils;

/**
 * Utility class for a "safer" sleep
 */
@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
public class Sleeper {
    /**
     * Runs a synchronized based wait mechanism instead of sleep
     *
     * @param timeoutMillis
     */
    public static void sleep(long timeoutMillis) {
        try {
            Object obj = new Object();
            synchronized (obj) {
                obj.wait(timeoutMillis);
            }
        } catch (Exception ex) {

        }
    }
}
