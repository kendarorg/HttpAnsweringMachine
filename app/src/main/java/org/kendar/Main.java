package org.kendar;

import org.kendar.servers.AnsweringServer;
import org.kendar.servers.JsonConfiguration;
import org.kendar.utils.FakeFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootApplication
public class Main implements CommandLineRunner{
    private static final int MAX_THREADS=10;
    @Autowired
    private ApplicationContext applicationContext;


    private static final Logger logger = LoggerFactory.getLogger(Main.class);

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

        var configuration = applicationContext.getBean(JsonConfiguration.class);
        try {
            configuration.loadConfiguration("external.json");
        } catch (Exception e) {
            e.printStackTrace();
        }

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
}
