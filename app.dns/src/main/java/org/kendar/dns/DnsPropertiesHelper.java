package org.kendar.dns;

import org.kendar.servers.dns.DnsServerDescriptor;
import org.kendar.utils.PropertiesHelper;
import org.kendar.utils.PropertiesManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DnsPropertiesHelper extends PropertiesHelper {
    private Environment environment;

    public DnsPropertiesHelper(Environment environment, ApplicationContext applicationContext){
        this.environment = environment;
        this.applicationContext = applicationContext;
    }

    private String dnsPath;
    private ApplicationContext applicationContext;

    @Override
    public void loadProperties(Map<String, Object> propMap, PropertiesManager propertiesManager) {
        dnsPath = environment.getProperty("dns.path");
        if(dnsPath!=null && !dnsPath.equalsIgnoreCase("null")){
            var prop = propertiesManager.loadPropertiesFile(dnsPath);
            if(prop!=null){
                int i=0;
                for(;i<1000;i++){
                    var index = "dns.resolve."+Integer.toString(i);
                    var dns = prop.getProperty(index);
                    if(dns != null){
                        propMap.put(index,dns);
                    }else{
                        break;
                    }
                }
                //Add terminator
                propMap.put("dns.resolve."+Integer.toString(i+1),null);
            }
        }
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String,String> result = new HashMap<>();
        var answeringHttpsServer = applicationContext.getBean(DnsMultiResolverImpl.class);
        //addIfNotNull(result,"simpleproxy.path",environment);
        List<DnsServerDescriptor> extraDomains = answeringHttpsServer.getExtraServers();
        for (int i = 0; i < extraDomains.size(); i++) {
            var storedName = extraDomains.get(i).getName()!=null?extraDomains.get(i).getName():extraDomains.get(i).getIp();
            result.put("dns.resolve."+i,storedName);
        }

        return result;
    }
}
