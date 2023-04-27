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
import java.util.Calendar;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
public class SimpleProxyHandlerImpl implements SimpleProxyHandler {
    private static final String HTTP = "http";
    private static final String HTTPS = "http";
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
        eventQueue.register(this::handleConfigChange, ProxyConfigChanged.class);
    }

    public void handleConfigChange(ProxyConfigChanged event) {
        verifyProxyConfiguration();
    }

    @PostConstruct
    public void init() {

        scheduler.scheduleAtFixedRate(
                () -> {
                    doLog();
                    verifyProxyConfiguration();
                },
                1000L,
                1000L,
                TimeUnit.MILLISECONDS);

        logger.info("Simple proxies LOADED");
    }

    private class ProxyPollTiming {
        public long lastTimeCheck;
        public String id;
        public boolean lastStatus;
    }

    private final ConcurrentHashMap<String, ProxyPollTiming> pollTiming = new ConcurrentHashMap<>();

    private final AtomicBoolean running = new AtomicBoolean(false);

    private void verifyProxyConfiguration() {
        if (running.get()) return;
        running.set(true);
        try {
            var config = configuration.getConfiguration(SimpleProxyConfig.class).copy();

            var changed = false;


            for (int i = 0; i < config.getProxies().size(); i++) {
                var now = Calendar.getInstance().getTimeInMillis();
                var currentProxy = config.getProxies().get(i);
                if (currentProxy.isForce()) {
                    continue;
                }
                if (!pollTiming.containsKey(currentProxy.getId())) {
                    var pt = new ProxyPollTiming();
                    pt.lastStatus = false;
                    pt.lastTimeCheck = -1L;
                    pollTiming.put(currentProxy.getId(), pt);
                }
                var pt = pollTiming.get(currentProxy.getId());
                if (pt.lastTimeCheck < (now - 1000) && currentProxy.isRunning() == false) {
                    changed = checkRemoteMachines(currentProxy) || changed;
                    if (pt.lastStatus != currentProxy.isRunning()) {
                        logger.info("Proxy {} now active", currentProxy.getWhen());
                    }
                    pt.lastStatus = currentProxy.isRunning();
                    pt.lastTimeCheck = now;
                } else if (pt.lastTimeCheck < (now - 60000) && currentProxy.isRunning() == true) {
                    changed = checkRemoteMachines(currentProxy) || changed;

                    if (pt.lastStatus != currentProxy.isRunning()) {
                        logger.info("Proxy {} now inactive", currentProxy.getWhen());
                    }
                    pt.lastStatus = currentProxy.isRunning();
                    pt.lastTimeCheck = now;
                }
            }
            if (changed) {
                configuration.setConfiguration(config);
            }
            running.set(false);
        } catch (Exception ex) {
            running.set(false);
        }
    }

    private void doLog() {
        if (!startedOnce) {
            startedOnce = true;
            logger.info("Simple proxies CHECKED");
        }
    }

    private boolean isTcpPortAvailable(int port, String host) {
        try (var ignored = new Socket(host, port)) {
            return true;
        } catch (IOException e) {
            logger.trace(e.getMessage());
            return false;
        }
    }

    private boolean checkRemoteMachines(RemoteServerStatus value) {
        var explodedTestUrl = value.getTest().split(":");
        var data = multiResolver.resolveRemote(explodedTestUrl[0]);
        if (explodedTestUrl[0].equalsIgnoreCase("127.0.0.1") ||
                explodedTestUrl[0].equalsIgnoreCase("localhost")) {
            data.clear();
            data.add("127.0.0.1");
        }
        var oldStatus = value.isRunning();
        if (data != null && !data.isEmpty()) {
            try {
                var inetAddress = InetAddress.getByName(data.get(0));
                var running = inetAddress.isReachable(100);
                if (running) {
                    if (explodedTestUrl.length == 2) {
                        var port = Integer.parseInt(explodedTestUrl[1]);
                        running = isTcpPortAvailable(port, data.get(0));
                    } else {
                        running = isTcpPortAvailable(80, explodedTestUrl[0]) || isTcpPortAvailable(443, explodedTestUrl[0]);
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
            try (Socket t = new Socket(host, 7)) {
                DataInputStream dis = new DataInputStream(t.getInputStream());
                PrintStream ps = new PrintStream(t.getOutputStream());
                ps.println("Hello");
                @SuppressWarnings("deprecation")
                String str = dis.readLine();
                if (str.equals("Hello")) {
                    pingable = true;
                }
            }
            return pingable;
        } catch (IOException e) {
            return false;
        }
    }

    public Request translate(Request oriSource) throws MalformedURLException {

        var realSrc = oriSource.getProtocol() + "://" + oriSource.getHost() + oriSource.getPath();
        var config = configuration.getConfiguration(SimpleProxyConfig.class);
        var sorted = config.getProxies().stream()
                .sorted(Comparator.comparingInt(a -> ((RemoteServerStatus) a).getWhen().length()).reversed())
                .collect(Collectors.toList());
        for (RemoteServerStatus status : sorted) {
            if (realSrc.startsWith(status.getWhen()) && status.isRunning()) {
                var source = oriSource.copy();
                realSrc = realSrc.replace(status.getWhen(), status.getWhere());
                var url = new URL(realSrc);
                if (url.getProtocol().equalsIgnoreCase(HTTPS) && url.getPort() != 443) {
                    source.setPort(url.getPort());
                    source.setProtocol(HTTPS);
                } else if (url.getProtocol().equalsIgnoreCase(HTTP) && url.getPort() != 80) {
                    source.setPort(url.getPort());
                    source.setProtocol(HTTP);
                } else if (url.getProtocol().equalsIgnoreCase(HTTPS) && url.getPort() == 443) {
                    source.setPort(-1);
                    source.setProtocol(HTTPS);
                } else if (url.getProtocol().equalsIgnoreCase(HTTP) && url.getPort() == 80) {
                    source.setPort(-1);
                    source.setProtocol(HTTP);
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
