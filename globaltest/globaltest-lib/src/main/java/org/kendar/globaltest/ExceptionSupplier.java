package org.kendar.globaltest;

@FunctionalInterface
public interface ExceptionSupplier<T> {

    /**
     * Gets a result.
     *
     * @return a result
     */
    T get() throws Exception;
}