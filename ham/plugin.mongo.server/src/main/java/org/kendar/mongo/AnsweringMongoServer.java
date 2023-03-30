package org.kendar.mongo;

import org.kendar.events.EventQueue;
import org.kendar.events.ServiceStarted;
import org.kendar.servers.AnsweringServer;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.PluginsInitializer;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class AnsweringMongoServer implements AnsweringServer {
    private final JsonConfiguration configuration;
    private EventQueue eventQueue;
    private final Logger logger;
    private final MongoServerOld mongoServer;
    private boolean running = false;

    public AnsweringMongoServer(
            LoggerBuilder loggerBuilder,
            MongoServerOld mongoServer,
            JsonConfiguration configuration,
            PluginsInitializer pluginsInitializer,
            EventQueue eventQueue) {
        this.logger = loggerBuilder.build(AnsweringMongoServer.class);
        this.mongoServer = mongoServer;
        this.configuration = configuration;
        this.eventQueue = eventQueue;

        pluginsInitializer.addSpecialLogger(MongoQuery.class.getName(), "MongoDb Logging (DEBUG,TRACE)");
    }

    public void isSystem() {
        //To check if is system class
    }

    @Override
    public void run() {
        if (running) return;
        var config = configuration.getConfiguration(MongoConfig.class);
        if (!config.isActive()) return;
        running = true;

        try {

            eventQueue.handle(new ServiceStarted().withTye("mongo"));
            mongoServer.run(config.getPort(),this);
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
}
