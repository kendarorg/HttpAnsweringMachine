package org.kendar.servers.http;

import java.util.List;
import java.util.Map;

public interface PluginsInitializer {
    void addPluginAddress(String address, String description);

    void addSpecialLogger(String path, String description);

    Map<String, String> getPluginAddresses();

    List<SpecialLoggerDescriptor> getSpecialLoggers();

}
