package org.kendar.servers;

import com.sun.net.httpserver.HttpServer;
import org.kendar.events.EventQueue;
import org.kendar.events.ServiceStarted;
import org.kendar.servers.config.HttpWebServerConfig;
import org.kendar.servers.http.AnsweringHandler;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.Sleeper;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.Executors;

@Component
public class AnsweringHttpServer implements AnsweringServer {

    private final Logger logger;
    private final AnsweringHandler handler;
    private final JsonConfiguration configuration;
    private EventQueue eventQueue;
    private boolean running = false;
    private final HashMap<String, HttpServer> httpServers = new HashMap<>();

    public AnsweringHttpServer(
            LoggerBuilder loggerBuilder, AnsweringHandler handler, JsonConfiguration configuration, EventQueue eventQueue) {
        this.logger = loggerBuilder.build(AnsweringHttpServer.class);
        this.handler = handler;
        this.configuration = configuration;
        this.eventQueue = eventQueue;
    }

    public void isSystem() {
        //To check if is a system class
    }

    @Override
    public void run() {
        if (running) return;
        var config = configuration.getConfiguration(HttpWebServerConfig.class).copy();
        if (!config.isActive()) return;
        running = true;
        httpServers.clear();

        try {
            // setup the socket address
            var ports = config.getPort().split(";");
            for (var port : ports) {
                InetSocketAddress address = new InetSocketAddress(Integer.parseInt(port));

                // initialise the HTTPS server
                var httpServer = HttpServer.create(address, config.getBacklog());

                httpServer.createContext("/", handler);
                if (config.isUseCachedExecutor()) {
                    httpServer.setExecutor(Executors.newCachedThreadPool()); // creates a cached
                } else {
                    httpServer.setExecutor(null); // creates a default executor
                }

                httpServer.start();
                httpServers.put(port, httpServer);
                eventQueue.handle(new ServiceStarted().withTye("http"));
                logger.info("Http server LOADED, port: {}", port);
            }

            var localConfig = configuration.getConfiguration(HttpWebServerConfig.class);
            while (running && localConfig.isActive()) {
                Sleeper.sleep(10000);
                localConfig = configuration.getConfiguration(HttpWebServerConfig.class);
            }
        } catch (Exception ex) {
            logger.error(
                    "Failed to create HTTP server on port " + config.getPort() + " of localhost", ex);
        } finally {
            running = false;
        }
    }

    @Override
    public boolean shouldRun() {
        var localConfig = configuration.getConfiguration(HttpWebServerConfig.class);
        return localConfig.isActive() && !running;
    }

    public void stop() {
        for (var httpServer : httpServers.entrySet()) {
            httpServer.getValue().stop(0);
        }
        running = false;
        Sleeper.sleep(1000);
    }
}
