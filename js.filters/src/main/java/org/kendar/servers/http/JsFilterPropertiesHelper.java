package org.kendar.servers.http;

import org.kendar.utils.PropertiesHelper;
import org.kendar.utils.PropertiesManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class JsFilterPropertiesHelper extends PropertiesHelper {
    private final Environment environment;

    public JsFilterPropertiesHelper(Environment environment, ApplicationContext applicationContext){
        this.environment = environment;
    }
    @Override
    public void loadProperties(Map<String, Object> destination, PropertiesManager propertiesManager) {

    }

    @Override
    public Map<String, String> getProperties() {
        Map<String,String> result = new HashMap<>();
        addIfNotNull(result,"jsfilter.path",environment);
        return result;
    }
}
