package org.kendar.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Component
public class PropertiesManagerImpl implements PropertiesManager{
    @Autowired
    private Environment environment;
    private List<PropertiesHelper> propertiesHelperList;

    public PropertiesManagerImpl(Environment environment, List<PropertiesHelper> propertiesHelperList){
        this.environment = environment;
        this.propertiesHelperList = propertiesHelperList;
    }

    public Properties loadPropertiesFile(String path){
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(path)) {
            // load a properties file
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return prop;
    }

    @Override
    public void savePropertiesFile(String path) {
        String overriderProperty = getExternalPropertyFile();
        Properties prop = new Properties();
        for(var propertyHelper:this.propertiesHelperList){
            for(var kvp : propertyHelper.getProperties().entrySet()){
                prop.setProperty(kvp.getKey(),kvp.getValue());
            }
        }
        try (OutputStream output = new FileOutputStream(overriderProperty)) {
            // save properties to project root folder
            prop.store(output, null);
            System.out.println(prop);

        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    private String getExternalPropertyFile() {
        var overriderProperty = System.getProperty("external.property"); //"dns.google";
        if(overriderProperty==null || overriderProperty.length()==0){
            overriderProperty = "external.properties";
        }
        if(overriderProperty==null || overriderProperty.length()==0 || !Files.exists(Path.of(overriderProperty))){
            overriderProperty=null;
        }
        return overriderProperty;
    }

    @PostConstruct
    public void init(){
        var overriderProperty = getExternalPropertyFile();

        MutablePropertySources propertySources = ((ConfigurableEnvironment)environment).getPropertySources();

        if(overriderProperty!=null){
            Map<String,Object> externalPropertyMap = new HashMap<>();
            var loadedPropertyFile = loadPropertiesFile(overriderProperty);
            for(var property: loadedPropertyFile.keySet()){
                externalPropertyMap.put(property.toString(),(String)loadedPropertyFile.getProperty(property.toString()));
            }
            propertySources.addFirst(new MapPropertySource("loaded.from.external", externalPropertyMap));
        }
        Map<String,Object> realPropertyMap = new HashMap<>();
        //Load standard environment
        for(var propertyHelper:this.propertiesHelperList){
            propertyHelper.loadProperties(realPropertyMap,this);
        }
        propertySources.addFirst(new MapPropertySource("loaded.from.plugins", realPropertyMap));



    }
}
