package org.kendar;

import org.kendar.servers.AnsweringServer;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.GlobalConfig;
import org.kendar.utils.FakeFuture;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootApplication
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class Main implements CommandLineRunner {
  private static final int MAX_THREADS = 10;
  @Autowired private ApplicationContext applicationContext;

  public static void main(String[] args) throws IOException {
    javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
            (hostname, sslSession) -> true);
    SpringApplication app = new SpringApplication(Main.class);
    app.setLazyInitialization(true);
    app.run(args);
  }


  @SuppressWarnings("InfiniteLoopStatement")
  @Override
  public void run(String... args) {
    var executor = Executors.newFixedThreadPool(MAX_THREADS);

    var configuration = loadConfigurationFile();
    setupLogging(configuration);

    var answeringServers = applicationContext.getBeansOfType(AnsweringServer.class);

    var logger = applicationContext.getBean(LoggerBuilder.class).build(this.getClass());
    Map<AnsweringServer, Future<?>> futures = setupFakeFutures(answeringServers);

    while (true) {
      intializeRunners(executor, futures);
      runRunners(executor, futures);
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        logger.trace(e.getMessage());
      }
    }
  }

  private Map<AnsweringServer, Future<?>> setupFakeFutures(
      Map<String, AnsweringServer> answeringServers) {
    Map<AnsweringServer, Future<?>> futures = new HashMap<>();
    for (AnsweringServer answeringServer : answeringServers.values()) {
      futures.put(answeringServer, new FakeFuture());
    }
    return futures;
  }

  private void runRunners(ExecutorService executor, Map<AnsweringServer, Future<?>> futures) {
    for (var future : futures.entrySet()) {
      if (future.getValue().isDone() || future.getValue().isCancelled()) {
        if (future.getKey().shouldRun()) {
          Future<?> f = executor.submit(future.getKey());
          futures.put(future.getKey(), f);
        }
      }
    }
  }

  private void intializeRunners(ExecutorService executor, Map<AnsweringServer, Future<?>> futures) {
    var logger = applicationContext.getBean(LoggerBuilder.class).build(this.getClass());
    for (var future : futures.entrySet()) {
      if (future.getValue().isDone() || future.getValue().isCancelled()) {
        try {
          if (future.getKey().getClass().getMethod("isSystem") != null) {
            if (future.getKey().shouldRun()) {
              Future<?> f = executor.submit(future.getKey());
              futures.put(future.getKey(), f);
            }
          }
        } catch (NoSuchMethodException e) {
          logger.trace(e.getMessage());
        }
      }
    }
  }

  private JsonConfiguration loadConfigurationFile() {
    var configuration = applicationContext.getBean(JsonConfiguration.class);
    try {
      var externalPath = System.getProperty("jsonconfig","external.json");
      configuration.loadConfiguration(externalPath);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return configuration;
  }

  private void setupLogging(JsonConfiguration configuration) {
    var loggerBuilder = applicationContext.getBean(LoggerBuilder.class);
    var globalConfig = configuration.getConfiguration(GlobalConfig.class);
    loggerBuilder.setLevel(Logger.ROOT_LOGGER_NAME, globalConfig.getLogging().getLogLevel());
    for (var logConf : globalConfig.getLogging().getLoggers().entrySet()) {
      loggerBuilder.setLevel(logConf.getKey(), logConf.getValue());
    }
  }
}
