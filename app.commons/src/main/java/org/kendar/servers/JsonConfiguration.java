package org.kendar.servers;

public interface JsonConfiguration {
    void setConfiguration(String id, Object data) throws Exception;
    <T> T getConfiguration(String id,Class<T> aClass) throws Exception;
    void loadConfiguration(String fileName) throws Exception;
}
