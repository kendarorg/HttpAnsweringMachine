package org.kendar.servers.http;

import java.util.Map;

public interface PluginsAddressesRecorder {
    void addPluginAddress(String address, String description);
    Map<String,String> getPluginAddresses();
}
