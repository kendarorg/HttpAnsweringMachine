package org.kendar;

import org.kendar.dns.configurations.DnsConfig;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.GlobalConfig;
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
import java.util.ArrayList;
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
        //MutablePropertySources propertySources = ((ConfigurableEnvironment)environment).getPropertySources();
        //Map<String,Object> propMap = new HashMap<>();
        var configuration = applicationContext.getBean(JsonConfiguration.class);
        try {
            DnsConfig config = new DnsConfig();
            config.setExtraServers(new ArrayList<>());
            config.setActive(true);
            config.setBlocked(new ArrayList<>());
            config.getBlocked().add("wpad.*");
            config.getBlocked().add("*.trafficmanager.net");
            config.setPort(53);

            GlobalConfig global = new GlobalConfig();
            global.setLocalAddress("localhost");

            configuration.setConfiguration(global);
            configuration.setConfiguration(config);
        } catch (Exception e) {
            e.printStackTrace();
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
