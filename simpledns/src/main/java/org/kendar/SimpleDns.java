package org.kendar;

import org.kendar.dns.DnsServer;
import org.kendar.servers.AnsweringServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class SimpleDns implements CommandLineRunner {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private Environment environment;

    public static void main(String[] args) throws Exception {

        if(System.getProperty("jdk.tls.acknowledgeCloseNotify")==null){
            //throw new Exception("SHOULD SET -Djdk.tls.acknowledgeCloseNotify=true");
        }

        //SpringApplication.run(SimpleDns.class, args);
        SpringApplication app = new SpringApplication(SimpleDns.class);
        app.setLazyInitialization(true);
        app.run(args);
    }

    @Override
    public void run(String... args){
        MutablePropertySources propertySources = ((ConfigurableEnvironment)environment).getPropertySources();
        Map<String,Object> propMap = new HashMap<>();
        var otherDnss = System.getProperty("other.dns"); //"dns.google";
        var extraServers = environment.getProperty("dns.extraServers");
        if(otherDnss!=null && otherDnss.length()>0){
            extraServers = extraServers+","+otherDnss;
            propMap.put("dns.extraServers",extraServers);
            propertySources.addFirst(new MapPropertySource("extraDns", propMap));
        }

        var dnsServer = (org.kendar.dns.DnsServer)applicationContext.getBean(org.kendar.dns.DnsServer.class);
        while(true){
            try {
                dnsServer.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
