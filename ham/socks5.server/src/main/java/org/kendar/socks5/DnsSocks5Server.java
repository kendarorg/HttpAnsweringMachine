package org.kendar.socks5;

import org.kendar.servers.AnsweringServer;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.HttpWebServerConfig;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import sockslib.common.methods.NoAuthenticationRequiredMethod;
import sockslib.server.SocksProxyServer;
import sockslib.server.SocksServerBuilder;

@Component
public class DnsSocks5Server implements AnsweringServer {
    private final Logger logger;
    private JsonConfiguration configuration;
    private DnsMultiResolver multiResolver;
    private boolean running = false;

    public DnsSocks5Server(DnsMultiResolver multiResolver, LoggerBuilder loggerBuilder, JsonConfiguration configuration){

        this.multiResolver = multiResolver;
        this.logger = loggerBuilder.build(DnsSocks5Server.class);
        this.configuration = configuration;
    }
    @Override
    public void run() {
        if (running) return;
        var config = configuration.getConfiguration(Socks5Config.class).copy();
        if (!config.isActive()) return;
        running = true;
        try{
            DnsSocks5Handler.multiResolver = multiResolver;
            SocksProxyServer proxyServer = SocksServerBuilder.newBuilder(DnsSocks5Handler.class).
                    setSocksMethods(new NoAuthenticationRequiredMethod()).setBindPort(1080).build();
            proxyServer.start();
            logger.info("Socks5 server LOADED, port: " + config.getPort());
            var localConfig = configuration.getConfiguration(Socks5Config.class);
            while (running && localConfig.isActive()) {
                Thread.sleep(10000);
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
