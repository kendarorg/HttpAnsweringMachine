package org.kendar.servers;

public interface JsonConfiguration {
  void setConfiguration(Object data);

  <T extends BaseJsonConfig> T getConfiguration(Class<T> aClass);

  void loadConfiguration(String fileName) throws Exception;

  void saveConfiguration(String fileName) throws Exception;

  String getValue(String varName);

  long getConfigurationTimestamp(Class aClass);
}
