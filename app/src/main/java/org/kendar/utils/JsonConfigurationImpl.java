package org.kendar.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.BaseJsonConfig;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.ConfigAttribute;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JsonConfigurationImpl implements JsonConfiguration {
    class ParsedConfig{
        public Object deserialized;
        public long timestamp;
    }
    public JsonConfigurationImpl(){
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    private ObjectMapper mapper = new ObjectMapper();
    private ConcurrentHashMap<String, JsonNode> configurations = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ParsedConfig> deserializedConfigurations = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Class,String> mappingStringClasses = new ConcurrentHashMap<>();

    public <T extends BaseJsonConfig> T getConfiguration(Class<T> aClass) {
        try {
            if(!mappingStringClasses.containsKey(aClass)){
                var attribute = aClass.getAnnotation(ConfigAttribute.class);
                mappingStringClasses.put(aClass,attribute.id());
            }
            var sanitizedId = mappingStringClasses.get(aClass);
            if (!deserializedConfigurations.containsKey(sanitizedId)) {
                var parsedObject = mapper.treeToValue(configurations.get(sanitizedId), aClass);
                var parsedConfig = new ParsedConfig();
                parsedConfig.deserialized = parsedObject;
                parsedConfig.timestamp = Calendar.getInstance().getTimeInMillis();
                deserializedConfigurations.put(sanitizedId, parsedConfig);
            }
            var founded = deserializedConfigurations.get(sanitizedId);
            return (T) founded.deserialized;
        }catch (Exception e){
            throw new RuntimeException();
        }
    }

    public void setConfiguration(Object data) {
        try {
            var aClass = data.getClass();
            if(!mappingStringClasses.containsKey(aClass)){
                var attribute = aClass.getAnnotation(ConfigAttribute.class);
                mappingStringClasses.put(aClass,attribute.id());
            }
            var sanitizedId = mappingStringClasses.get(aClass);
            var stringValue = mapper.writeValueAsString(data);
            var treeMap = mapper.readTree(stringValue);
            configurations.put(sanitizedId, treeMap);
            var parsedConfig = new ParsedConfig();
            parsedConfig.deserialized = data;
            parsedConfig.timestamp = Calendar.getInstance().getTimeInMillis();
            deserializedConfigurations.put(sanitizedId, parsedConfig);
        }catch(Exception ex){

        }
    }

    public void loadConfiguration(String fileName) throws Exception {
        var fullConfig = Files.readString(Path.of(fileName));
        var treeMap = mapper.readTree(fullConfig).elements();
        while (treeMap.hasNext()) {
            var pluginRoot = treeMap.next();
            var id = pluginRoot.get("id").asText().toLowerCase(Locale.ROOT);
            var system = false;
            if(pluginRoot.has("system")){
                system = pluginRoot.get("system").asBoolean();
            }
            configurations.put(id, pluginRoot);
        }
    }

    public void saveConfiguration(String fileName) throws Exception {
        var strings = new ArrayList<String>();

        for (var entry :configurations.entrySet()) {
            strings.add(mapper.writeValueAsString(entry.getValue()));
        }
        var mangledResult = "{"+String.join(",",strings)+"}";
        var parsedResult = mapper.readTree(mangledResult);
        var formattedResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsedResult);
        Files.writeString(Path.of(fileName),formattedResult);
    }

    @Override public String getValue(String varName) {
        var splitted = varName.split("\\.");
        var splittedIndex = 0;
        if(!configurations.containsKey(splitted[splittedIndex])) return null;
        var rootNode = configurations.get(splitted[splittedIndex]);
        splittedIndex++;
        while(splittedIndex<splitted.length && rootNode!=null){
            rootNode = rootNode.path(splitted[splittedIndex]);
            splittedIndex++;
        }
        if(rootNode!=null){
            return rootNode.textValue();
        }
        return null;
    }
}

