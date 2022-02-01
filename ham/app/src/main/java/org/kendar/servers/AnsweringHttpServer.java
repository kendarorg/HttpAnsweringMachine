package org.kendar.servers;

import com.sun.net.httpserver.HttpServer;
import org.kendar.servers.config.HttpWebServerConfig;
import org.kendar.servers.http.AnsweringHandler;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

@Component
public class AnsweringHttpServer implements AnsweringServer {

  private final Logger logger;
  private final AnsweringHandler handler;
  private final JsonConfiguration configuration;
  private boolean running = false;
  private HttpServer httpServer;

  public AnsweringHttpServer(
      LoggerBuilder loggerBuilder, AnsweringHandler handler, JsonConfiguration configuration) {
    this.logger = loggerBuilder.build(AnsweringHttpServer.class);
    this.handler = handler;
    this.configuration = configuration;
  }

  public void isSystem() {}

  @Override
  public void run() {
    if (running) return;
    var config = configuration.getConfiguration(HttpWebServerConfig.class).copy();
    if (!config.isActive()) return;
    running = true;

    try {
      // setup the socket address
      InetSocketAddress address = new InetSocketAddress(config.getPort());

      // initialise the HTTPS server
      httpServer = HttpServer.create(address, config.getBacklog());

      httpServer.createContext("/", handler);
      if (config.isUseCachedExecutor()) {
        httpServer.setExecutor(Executors.newCachedThreadPool()); // creates a cached
      } else {
        httpServer.setExecutor(null); // creates a default executor
      }

      httpServer.start();

      logger.info("Http server LOADED, port: " + config.getPort());
      var localConfig = configuration.getConfiguration(HttpWebServerConfig.class);
      while (running && localConfig.isActive()) {
        Thread.sleep(10000);
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
    httpServer.stop(1);
    running = false;

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
