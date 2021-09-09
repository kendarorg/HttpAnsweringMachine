package org.kendar.utils;

import org.springframework.core.env.Environment;

import java.util.Map;

public abstract class PropertiesHelper {
    public abstract void loadProperties(Map<String,Object> destination, PropertiesManager propertiesManager);
    public abstract Map<String,String> getProperties();
    protected void addIfNotNull(Map<String,String> outmap, String id, Environment environment){
        if(environment.getProperty(id)!=null){
            outmap.put(id,environment.getProperty(id));
        }
    }
}
