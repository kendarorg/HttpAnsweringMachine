package org.kendar.http;

import org.kendar.servers.AnsweringServer;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.socks5.Socks5Config;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class HttpsProxy implements AnsweringServer {
    private final Logger logger;
    private final LoggerBuilder loggerBuilder;
    private JsonConfiguration configuration;
    private DnsMultiResolver multiResolver;
    private boolean running = false;

    public HttpsProxy(DnsMultiResolver multiResolver, LoggerBuilder loggerBuilder, JsonConfiguration configuration) {

        this.multiResolver = multiResolver;
        this.logger = loggerBuilder.build(HttpsProxy.class);
        this.loggerBuilder = loggerBuilder;
        this.configuration = configuration;
    }

    @Override
    public void run() {
        if (running) return;
        var config = configuration.getConfiguration(Socks5Config.class).copy();
        if (!config.isActive()) return;
        running = true;
        try {
            var proxyHttp = new HttpsProxyImpl(config.getHttpProxyPort(), false, loggerBuilder, multiResolver);

            logger.info("Http/s proxy server LOADED, port: " + config.getHttpProxyPort());
            proxyHttp.listen();

        } catch (Exception ex) {

            logger.error(
                    "Failed to create Http/s server on port " + config.getHttpProxyPort() + " of localhost", ex);
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
