package org.kendar.dns;

import org.kendar.dns.configurations.DnsConfig;
import org.kendar.dns.configurations.ExtraDnsServer;
import org.kendar.dns.configurations.PatternItem;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.GlobalConfig;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component public class DnsMultiResolverImpl implements DnsMultiResolver {

  private final Pattern ipPattern = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");
  private final JsonConfiguration configuration;
  private final GlobalConfig globalConfig;
  private final ExecutorService executorService = Executors.newFixedThreadPool(20);
  private final Logger logger;
  private final String localHostAddress;
  private final Environment environment;
  private final Logger logQueries;
  private ConcurrentHashMap<String, HashSet<String>> localDomains = new ConcurrentHashMap<>();
  private PatternItem localDns;

  public DnsMultiResolverImpl(Environment environment, LoggerBuilder loggerBuilder, JsonConfiguration configuration) {
    this.environment = environment;
    this.logger = loggerBuilder.build(DnsMultiResolverImpl.class);
    this.logQueries = loggerBuilder.build(DnsQueries.class);
    this.localHostAddress = getLocalHostLANAddress();
    this.configuration = configuration;
    this.globalConfig = configuration.getConfiguration(GlobalConfig.class);
  }

  @PostConstruct public void init() {
    localDns = new PatternItem("id", globalConfig.getLocalAddress(), localHostAddress);
    var cloned = configuration.getConfiguration(DnsConfig.class).copy();
    String hostsFile = "";
    for (int i = 0; i < cloned.getResolved().size(); i++) {
      var record = cloned.getResolved().get(i);
      hostsFile += record.writeHostsLine() + "\n";
    }

    if (hostsFile.length() > 0) {
      try {
        var myWriter = new FileWriter("hosts.txt");
        myWriter.write(hostsFile);
        myWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    for (int i = 0; i < cloned.getExtraServers().size(); i++) {
      var extraServer = cloned.getExtraServers().get(i);
      extraServer.setEnv(false);
    }
    var otherDnss = System.getProperty("other.dns");
    if(otherDnss!=null && !otherDnss.isEmpty()){
      var splitted = otherDnss.split(",");
      for (var split : splitted) {
       var newExtra = new ExtraDnsServer();
       newExtra.setEnv(true);
       newExtra.setId(UUID.randomUUID().toString());
       newExtra.setAddress(split);
       cloned.getExtraServers().add(newExtra);
      }
    }
    for (int i = 0; i < cloned.getExtraServers().size(); i++) {
      var extraServer = cloned.getExtraServers().get(i);
      Matcher ipPatternMatcher = ipPattern.matcher(extraServer.getAddress());
      if (ipPatternMatcher.matches()) {
        extraServer.setResolved(extraServer.getAddress());
      } else {
        var namedDns = resolve(extraServer.getAddress(), false);
        if (namedDns.size() == 0) {
          logger.error("Not found named DNS " + extraServer.getAddress());
        } else {
          logger.info("Resolved named DNS " + extraServer.getAddress() + ":" + namedDns.get(0));

          extraServer.setResolved(namedDns.get(0));
        }
      }
    }
    configuration.setConfiguration(cloned);
  }

  private String getLocalHostLANAddress() {
    try {
      InetAddress candidateAddress = null;
      // Iterate all NICs (network interface cards)...
      for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
        NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
        // Iterate all IP addresses assigned to each card...
        for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
          InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
          if (!inetAddr.isLoopbackAddress()) {

            if (inetAddr.isSiteLocalAddress()) {
              // Found non-loopback site-local address. Return it immediately...
              return inetAddr.getHostAddress();
            } else if (candidateAddress == null) {
              // Found non-loopback address, but not necessarily site-local.
              // Store it as a candidate to be returned if site-local address is not subsequently found...
              candidateAddress = inetAddr;
              // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
              // only the first. For subsequent iterations, candidate will be non-null.
            }
          }
        }
      }
      if (candidateAddress != null) {
        // We did not find a site-local address, but we found some other non-loopback address.
        // Server might have a non-site-local address assigned to its NIC (or it might be running
        // IPv6 which deprecates the "site-local" concept).
        // Return this non-loopback candidate address...
        return candidateAddress.getHostAddress();
      }
      // At this point, we did not find a non-loopback address.
      // Fall back to returning whatever InetAddress.getLocalHost() returns...
      InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
      if (jdkSuppliedAddress == null) {
        return null;
      }
      return jdkSuppliedAddress.getHostAddress();
    } catch (Exception e) {
      return null;
    }
  }

  @Override public List<String> resolveLocal(String requestedDomain) {
    var config = configuration.getConfiguration(DnsConfig.class);
    var data = new ArrayList<String>();
    if (localDns.match(requestedDomain)) {
      data.add(localDns.getIp());
      return data;
    }
    if (requestedDomain.equalsIgnoreCase("localhost")) {
      data.add("127.0.0.1");
      return data;
    } else if (requestedDomain.endsWith("in-addr.arpa")) {
      data.add("127.0.0.1");
      return data;
    } else if (requestedDomain.endsWith("ip6.arpa")) {
      data.add("127.0.0.1");
      return data;
    } else {
      for (int i = 0; i < config.getResolved().size(); i++) {
        var item = config.getResolved().get(i);
        if (item.match(requestedDomain)) {
          if (logQueries.isDebugEnabled() || logQueries.isTraceEnabled()) {
            logger.info("Pattern " + item.getIp());
            logger.info("Request " + requestedDomain);
            logger.info("Ip " + item.getIp());

          }
          if (item.getIp().equalsIgnoreCase("127.0.0.1")) {
            data.add(this.localHostAddress);
          } else {
            data.add(item.getIp());
          }
        }
      }
    }
    var result = new ArrayList<>(data);
    if (logQueries.isDebugEnabled() || logQueries.isTraceEnabled()) {
      if (result.size() > 0) {
        logger.info("Resolved local " + requestedDomain + result.get(0));
      }
    }
    if (logQueries.isTraceEnabled()) {
      if (result.size() == 0) {
        logger.info("Unable to resolve locally " + requestedDomain);
      }
    }

    return data;
  }

  @Override public List<String> resolveRemote(String requestedDomain, boolean fromLocalHost) {
    var config = configuration.getConfiguration(DnsConfig.class);
    if (isBlockedDomainQuery(requestedDomain, config)) {
      return new ArrayList<>();
    }

    var data = new HashSet<String>();
    List<Callable<List<String>>> runnables = new ArrayList<>();
    var extraServersList = config.getExtraServers();
    for (int i = 0; i < extraServersList.size(); i++) {
      var serverToCall = extraServersList.get(i);
      if(!serverToCall.isEnabled()) continue;
      var runnable = new DnsRunnable(serverToCall.getResolved(), requestedDomain);
      runnables.add(runnable);
    }
    List<Future<List<String>>> futures = new ArrayList<>();
    try {
      futures = executorService.invokeAll(runnables);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    int finished = futures.size();
    //This method returns the time in millis
    long timeMilli = new Date().getTime();
    long timeEnd = timeMilli + 2000;

    while (finished != 0) {
      if (timeEnd <= new Date().getTime()) {
        //System.out.println("================");
        for (var current : futures) {
          current.cancel(true);
        }
        break;
      }
      finished = futures.size();
      for (var current : futures) {
        if (current.isCancelled()) {
          finished--;
        } else if (current.isDone()) {
          finished--;
          try {
            var currentData = current.get();
            if (currentData.size() == 0) {
              continue;
            }
            for (String address : current.get()) {
              if (!data.contains(address)) {
                data.add(address);
              }
            }
            for (var future : futures) {
              if (!future.isDone()) {
                future.cancel(true);
              }
            }
            futures.clear();
            if (data.size() > 0) {
              localDomains.put(requestedDomain, new HashSet<>(data));
            } else if (localDomains.containsKey(requestedDomain)) {
              localDomains.remove(requestedDomain);
            }
            return new ArrayList<>(data);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

      }
    }
    var result = new ArrayList<>(data);
    if (logQueries.isDebugEnabled() || logQueries.isTraceEnabled()) {
      if (result.size() > 0) {
        logger.info("Resloved remote " + requestedDomain + result.get(0));
      }
    }

    if (logQueries.isTraceEnabled()) {
      if (result.size() == 0) {
        logger.info("Unable to resolve locally " + requestedDomain);
      }
    }
    return result;
  }

  private boolean isBlockedDomainQuery(String requestedDomain, DnsConfig config) {
    var shouldBlock = false;
    List<String> configBlocked = config.getBlocked();
    for (int i = 0; i < configBlocked.size(); i++) {
      String blocked = configBlocked.get(i);
      if (blocked.endsWith("*")) {
        if (requestedDomain.startsWith(blocked.substring(0, blocked.length() - 1))) {
          shouldBlock = true;
        }
      } else if (blocked.startsWith("*")) {
        if (requestedDomain.endsWith(blocked.substring(1))) {
          shouldBlock = true;
        }
      } else if (requestedDomain.contains(blocked)) {
        shouldBlock = true;
      }
      if (shouldBlock) {
        break;
      }
    }
    return shouldBlock;
  }

  @Override public List<String> resolve(String requestedDomain, boolean fromLocalhost) {
    if (localDomains.containsKey(requestedDomain)) {
      return localDomains.get(requestedDomain).stream().collect(Collectors.toList());
    }

    var localData = resolveLocal(requestedDomain);
    if (localData.size() > 0) {
      return localData;
    }
    return resolveRemote(requestedDomain, fromLocalhost);
  }
}
