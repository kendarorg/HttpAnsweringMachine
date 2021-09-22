package org.kendar.servers.proxy;

import org.kendar.servers.AnsweringHttpsServer;
import org.kendar.utils.PropertiesHelper;
import org.kendar.utils.PropertiesManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SimpleProxyPropertiesHelper extends PropertiesHelper {
    private Environment environment;

    public SimpleProxyPropertiesHelper(Environment environment, ApplicationContext applicationContext){
        this.environment = environment;
        this.applicationContext = applicationContext;
    }

    private String simpleProxyPath;
    private ApplicationContext applicationContext;

    @Override
    public void loadProperties(Map<String, Object> propMap, PropertiesManager propertiesManager) {
        simpleProxyPath = environment.getProperty("simpleproxy.path");
        if(simpleProxyPath!=null && !simpleProxyPath.equalsIgnoreCase("null")){
            var prop = propertiesManager.loadPropertiesFile(simpleProxyPath);
            if(prop!=null){
                int i=0;
                for(;i<1000;i++){
                    var index = "simpleproxy."+Integer.toString(i)+".";
                    var when = prop.getProperty(index+"when");
                    if(when != null){
                        propMap.put(index+"when",when);
                        var where = prop.getProperty(index+"where");
                        propMap.put(index+"where",where);
                        var test = prop.getProperty(index+"test");
                        propMap.put(index+"test",test);
                    }else{
                        break;
                    }
                }

                //Add terminator
                propMap.put("simpleproxy."+Integer.toString(i+1)+"when",null);
            }
        }
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String,String> result = new HashMap<>();
        var answeringHttpsServer = applicationContext.getBean(SimpleProxyHandlerImpl.class);
        //addIfNotNull(result,"simpleproxy.path",environment);
        List<RemoteServerStatus> extraDomains = answeringHttpsServer.getProxies();
        extraDomains.sort((remoteServerStatus, t1) -> t1.getId());
        for (int i = 0; i < extraDomains.size(); i++) {
            RemoteServerStatus extraDomain = extraDomains.get(i);
            result.put("simpleproxy."+i+".when",extraDomain.getWhen());
            result.put("simpleproxy."+i+".where",extraDomain.getWhere());
            result.put("simpleproxy."+i+".test",extraDomain.getTest());
        }

        return result;
    }
}
