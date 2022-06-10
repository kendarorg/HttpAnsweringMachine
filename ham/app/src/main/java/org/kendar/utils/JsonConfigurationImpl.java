package org.kendar.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.events.EventQueue;
import org.kendar.events.events.ConfigChangedEvent;
import org.kendar.servers.BaseJsonConfig;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.SpecialJsonConfig;
import org.kendar.servers.config.ConfigAttribute;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class JsonConfigurationImpl implements JsonConfiguration {
  private final ObjectMapper mapper = new ObjectMapper();
  private final ConcurrentHashMap<String, JsonNode> configurations = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, ParsedConfig> deserializedConfigurations =
      new ConcurrentHashMap<>();

  @SuppressWarnings("rawtypes")
  private final ConcurrentHashMap<Class, String> mappingStringClasses = new ConcurrentHashMap<>();
  private final EventQueue eventQueue;
  private final Logger logger;

  public JsonConfigurationImpl(EventQueue eventQueue, LoggerBuilder loggerBuilder) {
    logger = loggerBuilder.build(JsonConfiguration.class);
    this.eventQueue = eventQueue;
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
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
      eventQueue.handle(evt);
    } catch (Exception ex) {
      logger.trace(ex.getMessage());
    }
  }

  public void setConfiguration(Object data) {
    setConfiguration(data, null);
  }

  public void loadConfiguration(String fileName) throws Exception {
    var filePath = Path.of(fileName);
    var fullConfig = Files.readString(filePath);
    var dirPath = filePath.toAbsolutePath().getParent();
    fullConfig = handleIncludes(fullConfig,dirPath);
    var treeMap = mapper.readTree(fullConfig).elements();
    while (treeMap.hasNext()) {
      var pluginRoot = treeMap.next();
      var id = pluginRoot.get("id").asText().toLowerCase(Locale.ROOT);

      configurations.put(id, pluginRoot);
    }
  }

  private String handleIncludes(String fullConfig, Path dirPath) throws IOException {
    var regex = "(?m)[\"']#include:([\\da-zA-Z_\\-\\.\\\\/]+)[\"']";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(fullConfig);
    var hasMatches = true;
    while(hasMatches){
      var hashSet = new HashSet<>();
      var iterations = 0;
      while(matcher.find()){
        iterations++;
        var group0 = matcher.group(0);
        var group1 = matcher.group(1);
        if(hashSet.contains(group0)){
          continue;
        }
        hashSet.add(group0);
        var includePath = Path.of(group1);
        if(!includePath.isAbsolute()){
          includePath = Path.of(dirPath.toString(),includePath.toString());

        }
        var includeFile = Files.readString(includePath);
        fullConfig = fullConfig.replaceAll(Pattern.quote(group0),includeFile);
      }
      //Re-check the match for extra infos
      if(iterations==0){
        break;
      }
      matcher = pattern.matcher(fullConfig);
    }
    return fullConfig;
  }

  public void saveConfiguration(String fileName) throws Exception {
   var formattedResult = getConfigurationAsString();
    Files.writeString(Path.of(fileName), formattedResult);
  }

  @Override
  public String getConfigurationAsString() throws Exception {
    var strings = new ArrayList<String>();

    for (var entry : configurations.entrySet()) {
      strings.add(mapper.writeValueAsString(entry.getValue()));
    }
    var mangledResult = "[" + String.join(",", strings) + "]";
    var parsedResult = mapper.readTree(mangledResult);
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsedResult);
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
