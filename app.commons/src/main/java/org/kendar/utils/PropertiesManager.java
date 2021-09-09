package org.kendar.utils;

import java.util.Properties;

public interface PropertiesManager {
    Properties loadPropertiesFile(String path);
    void savePropertiesFile(String path);
}
