package org.kendar.servers;

@SuppressWarnings("rawtypes") public interface JsonConfiguration {
  void setConfiguration(Object data);
  void setConfiguration(Object data,Runnable callback);

  <T extends BaseJsonConfig> T getConfiguration(Class<T> aClass);

  void loadConfiguration(String fileName) throws Exception;

  void saveConfiguration(String fileName) throws Exception;

  String getValue(String varName);

  long getConfigurationTimestamp(Class aClass);
}
