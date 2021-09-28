package org.kendar.servers.http;

import org.kendar.utils.PropertiesHelper;
import org.kendar.utils.PropertiesManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class HttpServersPropertiesHelper extends PropertiesHelper {
    private final Environment environment;

    public HttpServersPropertiesHelper(Environment environment, ApplicationContext applicationContext){
        this.environment = environment;
        this.applicationContext = applicationContext;
    }

    private String staticServersPath;
    private final ApplicationContext applicationContext;

    @Override
    public void loadProperties(Map<String, Object> propMap, PropertiesManager propertiesManager) {
        staticServersPath = environment.getProperty("staticserver.path");
        if(staticServersPath!=null && !staticServersPath.equalsIgnoreCase("null")){
            var prop = propertiesManager.loadPropertiesFile(staticServersPath);
            if(prop!=null){

                int i=0;
                for(;i<1000;i++){
                    var index = "staticserver."+Integer.toString(i)+".";
                    var address = prop.getProperty(index+"address");
                    if(address != null){
                        var path = prop.getProperty(index+"path");
                        propMap.put(index+"address",address);
                        var test = prop.getProperty(index+"test");
                        propMap.put(index+"path",path);
                    }else{
                        break;
                    }
                }

                //Add terminator
                propMap.put("staticserver."+Integer.toString(i+1)+"address",null);
            }
        }
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String,String> result = new HashMap<>();

        addIfNotNull(result,"https.port",environment);
        addIfNotNull(result,"https.enabled",environment);
        addIfNotNull(result,"https.backlog",environment);
        addIfNotNull(result,"https.useCachedExecutor",environment);

        addIfNotNull(result,"http.port",environment);
        addIfNotNull(result,"http.enabled",environment);
        addIfNotNull(result,"http.backlog",environment);
        addIfNotNull(result,"http.useCachedExecutor",environment);
        return result;
    }
}
