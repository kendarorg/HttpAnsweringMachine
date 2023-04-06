package org.kendar.mongo;

import org.kendar.events.EventQueue;
import org.kendar.events.ServiceStarted;
import org.kendar.mongo.config.MongoConfig;
import org.kendar.mongo.events.MongoConfigChanged;
import org.kendar.mongo.logging.MongoLogClient;
import org.kendar.mongo.logging.MongoLogServer;
import org.kendar.servers.AnsweringServer;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.PluginsInitializer;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AnsweringMongoServer implements AnsweringServer {
    private final JsonConfiguration configuration;
    private final EventQueue eventQueue;
    private final Logger logger;
    private final HamMongoServer hamMongoServer;
    private boolean running = false;

    public AnsweringMongoServer(
            LoggerBuilder loggerBuilder,
            HamMongoServer hamMongoServer,
            JsonConfiguration configuration,
            PluginsInitializer pluginsInitializer,
            EventQueue eventQueue) {
        this.logger = loggerBuilder.build(AnsweringMongoServer.class);
        this.hamMongoServer = hamMongoServer;
        this.configuration = configuration;
        this.eventQueue = eventQueue;

        pluginsInitializer.addSpecialLogger(MongoLogClient.class.getName(), "MongoDb Client Logging (DEBUG,TRACE)");
        pluginsInitializer.addSpecialLogger(MongoLogServer.class.getName(), "MongoDb Server Logging (DEBUG,TRACE)");
        eventQueue.register(this::handleConfigChange, MongoConfigChanged.class);
    }

    private void handleConfigChange(MongoConfigChanged t) {
        activeServers.clear();
    }

    public void isSystem() {
        //To check if is system class
    }

    private Map<Integer, HamMongoServer> activeServers = new ConcurrentHashMap<>();

    @Override
    public void run() {
        if (running) return;
        var config = configuration.getConfiguration(MongoConfig.class);
        if (!config.isActive()) return;
        running = true;

        try {
            activeServers.clear();
            eventQueue.handle(new ServiceStarted().withTye("mongo"));
            for(var single : config.getProxies()){
                var ms = hamMongoServer.clone();
                ms.run(single.getExposedPort(),this);
                activeServers.put(single.getExposedPort(),ms);

            }
            //TODO
            //mongoServer.run(77777,this);
        } catch (Exception e) {
            logger.error("Error running Mongo server", e);
        } finally {
            running = false;
        }
    }

    @Override
    public boolean shouldRun() {
        var localConfig = configuration.getConfiguration(MongoConfig.class);
        return localConfig.isActive() && !running;
    }

    public boolean isActive() {
        var localConfig = configuration.getConfiguration(MongoConfig.class);
        return localConfig.isActive();
    }
}
