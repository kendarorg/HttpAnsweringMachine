package org.kendar.servers.certificates.configurations;

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
public class CertificatesPropertiesHelper extends PropertiesHelper {
    private final Environment environment;

    public CertificatesPropertiesHelper(Environment environment, ApplicationContext applicationContext){
        this.environment = environment;
        this.applicationContext = applicationContext;
    }

    private String certificatesPath;
    private final ApplicationContext applicationContext;

    @Override
    public void loadProperties(Map<String, Object> propMap, PropertiesManager propertiesManager) {
        certificatesPath = environment.getProperty("https.certificates.path");
        if(certificatesPath!=null && !certificatesPath.equalsIgnoreCase("null")){
            var prop = propertiesManager.loadPropertiesFile(certificatesPath);
            if(prop!=null){
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
            }
        }
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String,String> result = new HashMap<>();
        var answeringHttpsServer = applicationContext.getBean(AnsweringHttpsServer.class);
        addIfNotNull(result,"https.certificates.cnname",environment);
        List<String> extraDomains = answeringHttpsServer.getExtraDomains();
        for (int i = 0; i < extraDomains.size(); i++) {
            String extraDomain = extraDomains.get(i);
            result.put("https.certificate."+i,extraDomain);
        }

        return result;
    }
}
