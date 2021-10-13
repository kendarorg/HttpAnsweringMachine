package org.kendar.servers.http;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PluginsAddressesRecorderImpl implements PluginsAddressesRecorder{
    private HashMap<String,String> plugins = new HashMap<>();
    @Override
    public void addPluginAddress(String address, String description) {
        if(null != address){
            plugins.put(address,description);
        }
    }

    @Override
    public Map<String, String> getPluginAddresses() {
        return new HashMap<>(plugins);
    }
}
