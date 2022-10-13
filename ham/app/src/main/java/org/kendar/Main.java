package org.kendar;

import org.kendar.servers.AnsweringServer;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.GlobalConfig;
import org.kendar.utils.FakeFuture;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.Sleeper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SpringBootApplication
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class Main implements CommandLineRunner {
  private static final int MAX_THREADS = 10;
  private boolean doRun = true;
  @Autowired private ApplicationContext applicationContext;

  public static void main(String[] args) {
    javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
            (hostname, sslSession) -> true);
    SpringApplication app = new SpringApplication(Main.class);
    app.setLazyInitialization(true);
    app.run(args);
  }

  @Override
  public void run(String... args) {
    var executor = Executors.newFixedThreadPool(MAX_THREADS);

    //Load the config file from json
    var configuration = loadConfigurationFile();
    //Prepare the configured loggin levels
    setupLogging(configuration);

    var answeringServers = applicationContext.getBeansOfType(AnsweringServer.class);

    //Create fake futures (terminated futures)
    Map<AnsweringServer, Future<?>> futures = setupFakeFutures(answeringServers);

    while (doRun) {
      //Prepare the runners that should ... well ... run
      initializeRunners(executor, futures);
      //Run everething
      runRunners(executor, futures);
      Sleeper.sleep(1000);
    }
  }

  public void stop(){
    doRun = false;
  }

  /**
   * This are created to initialize a series of processes that is not yet started
   * @param answeringServers
   * @return
   */
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
      if ((future.getValue().isDone() || future.getValue().isCancelled()) && future.getKey().shouldRun()) {
          Future<?> f = executor.submit(future.getKey());
          futures.put(future.getKey(), f);
      }
    }
  }

  private void initializeRunners(ExecutorService executor, Map<AnsweringServer, Future<?>> futures) {
    for (var future : futures.entrySet()) {
      if (future.getValue().isDone() || future.getValue().isCancelled()) {
          var isSystem = Arrays.stream(future.getKey().getClass().getMethods()).anyMatch(a->a.getName().equalsIgnoreCase("isSystem"));
          if (isSystem && future.getKey().shouldRun()) {

              Future<?> f = executor.submit(future.getKey());
              futures.put(future.getKey(), f);
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
    //loggerBuilder.setLevel("com.sun.net.httpserver",)
    loggerBuilder.setLevel(Logger.ROOT_LOGGER_NAME, globalConfig.getLogging().getLogLevel());
    for (var logConf : globalConfig.getLogging().getLoggers().entrySet()) {
      loggerBuilder.setLevel(logConf.getKey(), logConf.getValue());
    }
  }
}
