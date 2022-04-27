package org.kendar.servers.proxy;

import org.kendar.events.EventQueue;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
          JsonConfiguration configuration,
          EventQueue eventQueue) {
    this.multiResolver = multiResolver;
    this.configuration = configuration;
    logger = loggerBuilder.build(SimpleProxyHandlerImpl.class);
    eventQueue.register(this::handleConfigChange,ProxyConfigChanged.class);
  }

  public void handleConfigChange(ProxyConfigChanged event){
    verifyProxyConfiguration();
  }

  @PostConstruct
  public void init() {

    scheduler.scheduleAtFixedRate(
        () -> {
          doLog();
          verifyProxyConfiguration();
        },
        1000,
        60 * 1000,
        TimeUnit.MILLISECONDS);

    logger.info("Simple proxyes LOADED");
  }

  private void verifyProxyConfiguration() {
    var config = configuration.getConfiguration(SimpleProxyConfig.class);

    var changed = false;
    for (int i = 0; i < config.getProxies().size(); i++) {
      var currentProxy = config.getProxies().get(i);
      changed = checkRemoteMachines(currentProxy) ||changed;
    }
    if (changed) {
      configuration.setConfiguration(config);
    }
  }

  private void doLog() {
    if (!startedOnce) {
      startedOnce = true;
      logger.info("Simple proxyes CHECKED");
    }
  }

  private boolean isTcpPortAvailable(int port,String host) {
    Socket s = null;
    try
    {
      s = new Socket(host, port);
      return true;
    }
    catch (Exception e)
    {
      return false;
    }
    finally
    {
      if(s != null)
        try {s.close();}
        catch(Exception e){
          logger.trace(e.getMessage());
        }
    }
  }

  private boolean checkRemoteMachines(RemoteServerStatus value) {
    var explodedTestUrl = value.getTest().split(":");
    var data = multiResolver.resolveRemote(explodedTestUrl[0]);
    if(explodedTestUrl[0].equalsIgnoreCase("127.0.0.1")||
            explodedTestUrl[0].equalsIgnoreCase("localhost")){
      data.clear();
      data.add("127.0.0.1");
    }
    var oldStatus = value.isRunning();
    if (data != null && data.size() > 0) {
      try {
        var inetAddress = InetAddress.getByName(data.get(0));
        var running = inetAddress.isReachable(100);
        if(running) {
          if (explodedTestUrl.length == 2) {
            var port = Integer.parseInt(explodedTestUrl[1]);
            running = isTcpPortAvailable(port,data.get(0));
          }else{
            running = isTcpPortAvailable(80,explodedTestUrl[0]) || isTcpPortAvailable(443,explodedTestUrl[0]);
          }

        }

        value.setRunning(running);
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

  public Request translate(Request oriSource) throws MalformedURLException {

    var realSrc = oriSource.getProtocol() + "://" + oriSource.getHost() + oriSource.getPath();
    var config = configuration.getConfiguration(SimpleProxyConfig.class);
    for (int i = 0; i < config.getProxies().size(); i++) {
      var status = config.getProxies().get(i);
      if (realSrc.startsWith(status.getWhen()) && status.isRunning()) {
        var source = oriSource.copy();
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
        source.addOriginal(oriSource);
        return source;
      }
    }
    return oriSource;
  }
}
