package org.kendar.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.servers.JsonConfiguration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class JsonConfigurationImpl implements JsonConfiguration {
    private ObjectMapper mapper = new ObjectMapper();
    private ConcurrentHashMap<String, JsonNode> configurations = new ConcurrentHashMap<>();

    public <T> T getConfiguration(String id, Class<T> aClass) throws Exception {
        return mapper.treeToValue(configurations.get(id.toLowerCase(Locale.ROOT)), aClass);
    }

    public void setConfiguration(String id, Object data) throws Exception {
        var stringValue = mapper.writeValueAsString(data);
        var treeMap = mapper.readTree(stringValue);
        configurations.put(id.toLowerCase(Locale.ROOT),treeMap);
    }

    public void loadConfiguration(String fileName) throws Exception {
        var fullConfig = Files.readString(Path.of(fileName));
        var treeMap = mapper.readTree(fullConfig);
        while (treeMap.fieldNames().hasNext()) {
            var fieldName = treeMap.fieldNames().next().toLowerCase(Locale.ROOT);
            var configObject = treeMap.get(fieldName);
            if (fieldName.equalsIgnoreCase("plugins")) {
                var listOfPlugin = configObject.get("list");
                while (listOfPlugin.elements().hasNext()) {
                    var item = listOfPlugin.elements().next();
                    var id = item.get("id").asText().toLowerCase(Locale.ROOT);
                    var data = item.get("data");
                    configurations.put("plugins." + id, data);
                }
            } else {
                configurations.put(fieldName, configObject);
            }
        }
    }
}

