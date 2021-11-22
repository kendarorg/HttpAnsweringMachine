package org.kendar.servers.proxy;

import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.servers.http.Request;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.*;

@Component
public class SimpleProxyHandlerImpl implements SimpleProxyHandler {
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private final Logger logger;
  private final DnsMultiResolver multiResolver;
  private final JsonConfiguration configuration;
  private boolean startedOnce = false;

  public SimpleProxyHandlerImpl(
      LoggerBuilder loggerBuilder,
      DnsMultiResolver multiResolver,
      JsonConfiguration configuration) {
    this.multiResolver = multiResolver;
    this.configuration = configuration;
    logger = loggerBuilder.build(SimpleProxyHandlerImpl.class);
  }

  @PostConstruct
  public void init() {

    scheduler.scheduleAtFixedRate(
        () -> {
          doLog();
          var config = configuration.getConfiguration(SimpleProxyConfig.class);

          var changed = false;
          for (int i = 0; i < config.getProxies().size(); i++) {
            changed = changed || checkRemoteMachines(config.getProxies().get(i));
          }
          if (changed) {
            configuration.setConfiguration(config);
          }
        },
        1000,
        5 * 60 * 1000,
        TimeUnit.MILLISECONDS);

    logger.info("Simple proxyes LOADED");
  }

  private void doLog() {
    if (!startedOnce) {
      startedOnce = true;
      logger.info("Simple proxyes CHECKED");
    }
  }

  private boolean checkRemoteMachines(RemoteServerStatus value) {
    var data = multiResolver.resolveRemote(value.getTest(), false);
    var oldStatus = value.isRunning();
    if (data != null && data.size() > 0) {
      try {
        var inetAddress = InetAddress.getByName(data.get(0));
        value.setRunning(inetAddress.isReachable(100));
      } catch (IOException e) {
        value.setRunning(false);
      }
    } else {
      value.setRunning(false);
    }
    return isChanged(value, oldStatus);
  }

  private boolean isChanged(RemoteServerStatus value, boolean oldStatus) {
    return oldStatus != value.isRunning();
  }

  public boolean ping(String host) {
    try {
      var pingable = false;
      Socket t = new Socket(host, 7);
      DataInputStream dis = new DataInputStream(t.getInputStream());
      PrintStream ps = new PrintStream(t.getOutputStream());
      ps.println("Hello");
      @SuppressWarnings("deprecation")
      String str = dis.readLine();
      if (str.equals("Hello")) {
        pingable = true;
      }
      t.close();
      return pingable;
    } catch (IOException e) {
      return false;
    }
  }

  public Request translate(Request source) throws MalformedURLException {
    var realSrc = source.getProtocol() + "://" + source.getHost() + source.getPath();
    var config = configuration.getConfiguration(SimpleProxyConfig.class);
    for (int i = 0; i < config.getProxies().size(); i++) {
      var status = config.getProxies().get(i);
      if (realSrc.startsWith(status.getWhen()) && status.isRunning()) {
        realSrc = realSrc.replace(status.getWhen(), status.getWhere());
        var url = new URL(realSrc);
        if (url.getProtocol().equalsIgnoreCase("https") && url.getPort() != 443) {
          source.setPort(url.getPort());
          source.setProtocol("https");
        } else if (url.getProtocol().equalsIgnoreCase("http") && url.getPort() != 80) {
          source.setPort(url.getPort());
          source.setProtocol("http");
        } else if (url.getProtocol().equalsIgnoreCase("https") && url.getPort() == 443) {
          source.setPort(-1);
          source.setProtocol("https");
        } else if (url.getProtocol().equalsIgnoreCase("http") && url.getPort() == 80) {
          source.setPort(-1);
          source.setProtocol("http");
        }
        source.setHost(url.getHost());
        source.setPath(url.getPath());
        return source;
      }
    }
    return source;
  }
}
