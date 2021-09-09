package org.kendar.servers.derby;

import org.kendar.utils.PropertiesHelper;
import org.kendar.utils.PropertiesManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DerbyPropertiesHelper extends PropertiesHelper {
    private Environment environment;
    private ApplicationContext applicationContext;

    public DerbyPropertiesHelper(Environment environment, ApplicationContext applicationContext){
        this.environment = environment;
        this.applicationContext = applicationContext;
    }
    private int derbyPort;
    private boolean derbyEnabled;

    @Override
    public void loadProperties(Map<String, Object> destination, PropertiesManager propertiesManager) {
        var derbyPortString = environment.getProperty("derby.port");
        derbyPort = derbyPortString==null?1527:Integer.parseInt(derbyPortString);
        var derbyEnableString = environment.getProperty("derby.enabled");
        derbyEnabled = derbyEnableString==null?false:Boolean.parseBoolean(derbyEnableString);
        if(derbyEnabled){
            destination.put("derby.driver","org.apache.derby.jdbc.ClientDriver");
            destination.put("derby.url","jdbc:derby://localhost:"+ derbyPort);
        }
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String,String> result = new HashMap<>();
        addIfNotNull(result,"derby.port",environment);
        addIfNotNull(result,"derby.enabled",environment);
        addIfNotNull(result,"derby.root.user",environment);
        addIfNotNull(result,"derby.root.password",environment);
        addIfNotNull(result,"derby.path",environment);
        return result;
    }
}
