package org.kendar.socks5;

import ch.qos.logback.classic.Level;
import org.kendar.servers.AnsweringServer;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.Sleeper;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import sockslib.common.methods.NoAuthenticationRequiredMethod;
import sockslib.server.SocksProxyServer;
import sockslib.server.SocksServerBuilder;

@Component
public class Socks5Proxy implements AnsweringServer {
    private final Logger logger;
    private final JsonConfiguration configuration;
    private final DnsMultiResolver multiResolver;
    private boolean running = false;

    public Socks5Proxy(DnsMultiResolver multiResolver, LoggerBuilder loggerBuilder, JsonConfiguration configuration) {

        this.multiResolver = multiResolver;
        this.logger = loggerBuilder.build(Socks5Proxy.class);
        this.configuration = configuration;
        loggerBuilder.setLevel("sockslib.server.io.SocketPipe", Level.ERROR);
        loggerBuilder.setLevel("sockslib.server.BasicSocksProxyServer", Level.ERROR);
    }

    @Override
    public void run() {
        if (running) return;
        var config = configuration.getConfiguration(Socks5Config.class).copy();
        if (!config.isActive()) return;
        running = true;
        try {
            DnsSocks5Handler.multiResolver = multiResolver;
            SocksProxyServer proxyServer = SocksServerBuilder.newBuilder(DnsSocks5Handler.class).
                    setSocksMethods(new NoAuthenticationRequiredMethod()).setBindPort(config.getPort()).build();
            proxyServer.start();
            logger.info("Socks5 server LOADED, port: " + config.getPort());
            var localConfig = configuration.getConfiguration(Socks5Config.class);
            while (running && localConfig.isActive()) {
                Sleeper.sleep(1000);
                localConfig = configuration.getConfiguration(Socks5Config.class);
            }
            proxyServer.shutdown();
        } catch (Exception ex) {
            logger.error(
                    "Failed to create Socks5 server on port " + config.getPort() + " of localhost", ex);
        } finally {
            running = false;
        }
    }


    @Override
    public boolean shouldRun() {
        var localConfig = configuration.getConfiguration(Socks5Config.class);
        return localConfig.isActive() && !running;
    }
}
