package org.kendar.oidc;

import org.kendar.utils.PropertiesHelper;
import org.kendar.utils.PropertiesManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OidcPropertiesHelper extends PropertiesHelper {
    private Environment environment;

    public OidcPropertiesHelper(Environment environment, ApplicationContext applicationContext){
        this.environment = environment;
    }
    @Override
    public void loadProperties(Map<String, Object> destination, PropertiesManager propertiesManager) {

    }

    @Override
    public Map<String, String> getProperties() {
        Map<String,String> result = new HashMap<>();
        addIfNotNull(result,"oidc.token.expiration",environment);
        return result;
    }
}
