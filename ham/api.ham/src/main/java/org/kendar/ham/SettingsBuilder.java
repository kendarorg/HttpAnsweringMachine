package org.kendar.ham;

public interface SettingsBuilder {
    void upload(String value) throws HamException;

    String download() throws HamException;
}
