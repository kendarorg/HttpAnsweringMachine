package org.kendar;

import org.kendar.servers.AnsweringServer;
import org.kendar.utils.FakeFuture;
import org.kendar.utils.PropertiesManager;
import org.kendar.utils.PropertiesManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootApplication
public class Main implements CommandLineRunner{
    private static final int MAX_THREADS=10;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private Environment environment;


    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        if(System.getProperty("jdk.tls.acknowledgeCloseNotify")==null){
            //throw new Exception("SHOULD SET -Djdk.tls.acknowledgeCloseNotify=true");
        }

        SpringApplication app = new SpringApplication(Main.class);
        app.setLazyInitialization(true);
        app.run(args);
    }

    @Override
    public void run(String... args){
        var executor = Executors.newFixedThreadPool(MAX_THREADS);
        var overriderProperty = System.getProperty("external.property"); //"dns.google";
        if(overriderProperty!=null && overriderProperty.length()>0 && Files.exists(Path.of(overriderProperty))){
            loadExternalProperty(overriderProperty);
        }
        applicationContext.getBeansOfType(PropertiesManager.class);

        var answeringServers = applicationContext.getBeansOfType(AnsweringServer.class);
        Map<AnsweringServer, Future<?>> futures = new HashMap();
        for(AnsweringServer answeringServer: answeringServers.values()){
            futures.put(answeringServer, new FakeFuture());
        }
        while(true){
            for(var future : futures.entrySet()){
                if(future.getValue().isDone()||future.getValue().isCancelled()){
                    try {
                        if(future.getKey().getClass().getMethod("isSystem")!=null) {
                            if (future.getKey().shouldRun()) {
                                Future<?> f = executor.submit(future.getKey());
                                futures.put(future.getKey(), f);
                            }
                        }
                    } catch (NoSuchMethodException e) {

                    }
                }
            }
            for(var future : futures.entrySet()){
                if(future.getValue().isDone()||future.getValue().isCancelled()){
                    if(future.getKey().shouldRun()){
                        Future<?> f = executor.submit(future.getKey());
                        futures.put(future.getKey(),f);
                    }
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }
    }

    private void loadExternalProperty(String overriderProperty) {

        try {
            MutablePropertySources propertySources = ((ConfigurableEnvironment) environment).getPropertySources();
            Map<String, Object> propMap = new HashMap<>();
            var file = new FileInputStream(overriderProperty);
            Properties prop = new Properties();

            //load all the properties from this file
            prop.load(file);
            for (var singleProperty :prop.entrySet()) {
                propMap.put((String)singleProperty.getKey(),singleProperty.getValue());
            }
            propMap.put("external.property",overriderProperty);
            propertySources.addFirst(new MapPropertySource("external.property", propMap));
        }catch(Exception ex){

        }
    }
}