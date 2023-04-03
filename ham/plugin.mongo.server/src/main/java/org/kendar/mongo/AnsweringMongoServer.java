package org.kendar.mongo;

import org.kendar.events.EventQueue;
import org.kendar.events.ServiceStarted;
import org.kendar.mongo.config.MongoConfig;
import org.kendar.servers.AnsweringServer;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.PluginsInitializer;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class AnsweringMongoServer implements AnsweringServer {
    private final JsonConfiguration configuration;
    private final EventQueue eventQueue;
    private final Logger logger;
    private final MongoServer mongoServer;
    private boolean running = false;

    public AnsweringMongoServer(
            LoggerBuilder loggerBuilder,
            MongoServer mongoServer,
            JsonConfiguration configuration,
            PluginsInitializer pluginsInitializer,
            EventQueue eventQueue) {
        this.logger = loggerBuilder.build(AnsweringMongoServer.class);
        this.mongoServer = mongoServer;
        this.configuration = configuration;
        this.eventQueue = eventQueue;

        pluginsInitializer.addSpecialLogger(MongoLogClient.class.getName(), "MongoDb Client Logging (DEBUG,TRACE)");
        pluginsInitializer.addSpecialLogger(MongoLogServer.class.getName(), "MongoDb Server Logging (DEBUG,TRACE)");
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
            //TODO
            mongoServer.run(77777,this);
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
