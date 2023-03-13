package org.kendar.utils;

import org.kendar.servers.JsonConfiguration;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class SettingsDownloadUpload implements FullDownloadUpload {
    private final JsonConfiguration configuration;

    public SettingsDownloadUpload(JsonConfiguration configuration) {

        this.configuration = configuration;
    }

    @Override
    public Map<String, byte[]> retrieveItems() throws Exception {
        var data = configuration.getConfigurationAsString();
        var result = new HashMap<String, byte[]>();
        result.put("external.json", data.getBytes(StandardCharsets.UTF_8));
        return result;
    }

    @Override
    public String getId() {
        return "main";
    }

    @Override
    public void uploadItems(HashMap<String, byte[]> data) {
        var settings = data.get("external.json");
        if (settings != null) {
            configuration.setConfigurationAsString(new String(settings));
        }
    }
}
