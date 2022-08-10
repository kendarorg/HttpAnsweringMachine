package org.kendar.servers;

import org.kendar.dns.DnsQueries;
import org.kendar.dns.DnsServer;
import org.kendar.dns.configurations.DnsConfig;
import org.kendar.servers.http.PluginsInitializer;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AnsweringDnsServer implements AnsweringServer {
  private final JsonConfiguration configuration;
  private final Logger logger;
  private final DnsServer dnsServer;
  private boolean running = false;

  public AnsweringDnsServer(
          LoggerBuilder loggerBuilder,
          DnsServer dnsServer,
          JsonConfiguration configuration,
          PluginsInitializer pluginsInitializer) {
    this.logger = loggerBuilder.build(AnsweringDnsServer.class);
    this.dnsServer = dnsServer;
    this.configuration = configuration;

    pluginsInitializer.addSpecialLogger(DnsQueries.class.getName(), "DNS Logging (DEBUG,TRACE)");
  }

  public void isSystem() {
    //To check if is system class
  }

  @Override
  public void run() {
    if (running) return;
    var config = configuration.getConfiguration(DnsConfig.class);
    if (!config.isActive()) return;
    running = true;

    try {
      dnsServer.run();
    } catch (IOException | InterruptedException e) {
      logger.error("Error running DNS server",e);
    } finally {
      running = false;
    }
  }

  @Override
  public boolean shouldRun() {
    var localConfig = configuration.getConfiguration(DnsConfig.class);
    return localConfig.isActive() && !running;
  }
}
