package org.kendar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.events.events.ConfigChangedEvent;
import org.kendar.servers.BaseJsonConfig;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.SpecialJsonConfig;
import org.kendar.servers.config.ConfigAttribute;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class JsonConfigurationImpl implements JsonConfiguration {
    private final ObjectMapper mapper = new ObjectMapper();
    private final ConcurrentHashMap<String, JsonNode> configurations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ParsedConfig> deserializedConfigurations =
            new ConcurrentHashMap<>();

    @SuppressWarnings("rawtypes")
    private final ConcurrentHashMap<Class, String> mappingStringClasses = new ConcurrentHashMap<>();

    private final Logger logger;

    public JsonConfigurationImpl(LoggerBuilder loggerBuilder) {
        logger = loggerBuilder.build(JsonConfiguration.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends BaseJsonConfig> T getConfiguration(Class<T> aClass) {
        try {
            if (!mappingStringClasses.containsKey(aClass)) {
                var attribute = aClass.getAnnotation(ConfigAttribute.class);
                mappingStringClasses.put(aClass, attribute.id());
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
            if(founded==null || founded.deserialized==null){
                return handleMissing(aClass);
            }
            return (T) founded.deserialized;
        } catch (Exception e) {
            return handleMissing(aClass);
        }
    }

    private <T extends BaseJsonConfig> T handleMissing(Class<T> aClass) {
        logger.warn("Missing configuration "+ aClass.getName()+  " going default");
        var nopars = Arrays.stream(aClass.getConstructors()).
                filter(c->c.getParameterCount()==0).collect(Collectors.toList());
        if(nopars.isEmpty()){
            logger.error(aClass.getName()+" Must have default constructor");
            throw new RuntimeException(aClass.getName()+" Must have default constructor");
        }
        try {
            return (T) nopars.get(0).newInstance(new Object[]{});
        } catch (InstantiationException|IllegalAccessException|InvocationTargetException ex) {
            logger.error(aClass.getName()+" Must have default constructor");
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public long getConfigurationTimestamp(Class aClass) {
        if (!mappingStringClasses.containsKey(aClass)) {
            var attribute = (ConfigAttribute) aClass.getAnnotation(ConfigAttribute.class);
            mappingStringClasses.put(aClass, attribute.id());
        }
        var sanitizedId = mappingStringClasses.get(aClass);
        var item = deserializedConfigurations.get(sanitizedId);
        return item.timestamp;
    }

    @Override
    public void setConfigurationAsString(String body) {

    }

    @SuppressWarnings("rawtypes")
    public void setConfiguration(Object data, Runnable runnable) {
        try {
            var aClass = data.getClass();
            if (!mappingStringClasses.containsKey(aClass)) {
                var attribute = aClass.getAnnotation(ConfigAttribute.class);
                mappingStringClasses.put(aClass, attribute.id());
            }
            var sanitizedId = mappingStringClasses.get(aClass);

            if (data instanceof SpecialJsonConfig) {
                var toSaveData = (SpecialJsonConfig) ((BaseJsonConfig) data).copy();
                toSaveData.preSave();
                var stringValue = mapper.writeValueAsString(toSaveData);
                var treeMap = mapper.readTree(stringValue);
                configurations.put(sanitizedId, treeMap);
            } else {
                var stringValue = mapper.writeValueAsString(data);
                var treeMap = mapper.readTree(stringValue);
                configurations.put(sanitizedId, treeMap);
            }
            var parsedConfig = new ParsedConfig();
            parsedConfig.deserialized = data;
            parsedConfig.timestamp = Calendar.getInstance().getTimeInMillis();
            deserializedConfigurations.put(sanitizedId, parsedConfig);
            runnable.run();
            var evt = new ConfigChangedEvent();
            evt.setName(aClass.getName());
        } catch (Exception ex) {
            logger.trace(ex.getMessage());
        }
    }

    public void setConfiguration(Object data) {
        setConfiguration(data, null);
    }

    public void loadConfiguration(String fileName) throws Exception {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    public void saveConfiguration(String fileName) throws Exception {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    @Override
    public String getConfigurationAsString() throws Exception {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    @Override
    public String getValue(String varName) {
        var splitted = varName.split("\\.");
        var splittedIndex = 0;
        if (!configurations.containsKey(splitted[splittedIndex])) return null;
        var rootNode = configurations.get(splitted[splittedIndex]);
        splittedIndex++;
        while (splittedIndex < splitted.length && rootNode != null) {
            rootNode = rootNode.path(splitted[splittedIndex]);
            splittedIndex++;
        }
        if (rootNode != null) {
            return rootNode.textValue();
        }
        return null;
    }

    static class ParsedConfig {
        public Object deserialized;
        public long timestamp;
    }
}
