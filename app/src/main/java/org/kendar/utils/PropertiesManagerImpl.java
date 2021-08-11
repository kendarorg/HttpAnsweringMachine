package org.kendar.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class PropertiesManagerImpl implements PropertiesManager{
    @Autowired
    private Environment environment;
    @Value("${dns.path:null}")
    private String dnsPath;
    @Value("${https.certificates.path:null}")
    private String certificatesPath;
    @Value("${simpleproxy.path:null}")
    private String simpleProxyPath;
    @Value("${staticserver.path:null}")
    private String staticServersPath;
    @Value("${derby.port:1527}")
    private int derbyPort;
    @Value("${derby.enable:false}")
    private boolean derbyEnabled;

    public PropertiesManagerImpl(Environment environment){
        this.environment = environment;
    }

    private Properties loadPropertiesFile(String path){
        Properties prop = null;
        try (InputStream input = new FileInputStream(path)) {

            prop = new Properties();

            // load a properties file
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return prop;
    }

    @PostConstruct
    public void init(){
        MutablePropertySources propertySources = ((ConfigurableEnvironment)environment).getPropertySources();
        if(derbyEnabled){
            Map<String,Object> propMap = new HashMap<>();
            propMap.put("derby.driver","org.apache.derby.jdbc.ClientDriver");
            propMap.put("derby.url","jdbc:derby://localhost:"+ derbyPort);
            propertySources.addFirst(new MapPropertySource("derby.calculated", propMap));
        }

        if(dnsPath!=null && !dnsPath.equalsIgnoreCase("null")){
            var prop = loadPropertiesFile(dnsPath);
            if(prop!=null){
                Map<String,Object> propMap = new HashMap<>();
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
                propertySources.addFirst(new MapPropertySource("dns.resolve", propMap));
            }
        }

        if(certificatesPath!=null && !certificatesPath.equalsIgnoreCase("null")){
            var prop = loadPropertiesFile(certificatesPath);
            if(prop!=null){
                Map<String,Object> propMap = new HashMap<>();
                int i=0;
                for(;i<1000;i++){
                    var index = "https.certificate."+Integer.toString(i);
                    var dns = prop.getProperty(index);
                    if(dns != null){
                        propMap.put(index,dns);
                    }else{
                        break;
                    }
                }

                //Add terminator
                propMap.put("https.certificate."+Integer.toString(i+1),null);
                propertySources.addFirst(new MapPropertySource("https.certificate", propMap));
            }
        }

        if(simpleProxyPath!=null && !simpleProxyPath.equalsIgnoreCase("null")){
            var prop = loadPropertiesFile(simpleProxyPath);
            if(prop!=null){
                Map<String,Object> propMap = new HashMap<>();
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
                propertySources.addFirst(new MapPropertySource("https.certificate", propMap));
            }
        }

        if(staticServersPath!=null && !staticServersPath.equalsIgnoreCase("null")){
            var prop = loadPropertiesFile(staticServersPath);
            if(prop!=null){
                Map<String,Object> propMap = new HashMap<>();
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
                propertySources.addFirst(new MapPropertySource("staticservers", propMap));
            }
        }
    }
}
