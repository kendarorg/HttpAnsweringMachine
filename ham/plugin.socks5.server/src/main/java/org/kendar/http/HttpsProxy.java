package org.kendar.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import org.kendar.servers.AnsweringServer;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.HttpWebServerConfig;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.socks5.Socks5Config;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.Sleeper;
import org.littleshoot.proxy.*;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashSet;

@Component
public class HttpsProxy implements AnsweringServer {
    private final Logger logger;
    private final LoggerBuilder loggerBuilder;
    private final JsonConfiguration configuration;
    private final DnsMultiResolver multiResolver;
    private boolean running = false;
    private HttpProxyServer server;

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
        var httpConfig = configuration.getConfiguration(HttpWebServerConfig.class).copy();
        var spl = httpConfig.getPort().split(";");
        var intSet = new HashSet<Integer>();
        for (var sp : spl) {
            intSet.add(Integer.parseInt(sp));
        }
        if (!config.isActive()) return;
        running = true;
        try {
//            var proxyHttp = new HttpsProxyImpl(config.getHttpProxyPort(), false, loggerBuilder, multiResolver,
//                    config.isInterceptAllHttp());
//            proxyHttp.listen();

            server = DefaultHttpProxyServer.bootstrap()
                    .withPort(config.getHttpProxyPort())
                    .withAllowLocalOnly(false)
                    .withAllowRequestToOriginServer(true)
                    .plusActivityTracker(new ActivityTrackerAdapter())
                    .withTransparent(true)
                    .withFiltersSource(new HttpFiltersSourceAdapter() {
                        public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                            return new HttpFiltersAdapter(originalRequest) {
                                @Override
                                public InetSocketAddress proxyToServerResolutionStarted(String resolvingServerHostAndPort) {
                                    var config = configuration.getConfiguration(Socks5Config.class).copy();
                                    var hpp = resolvingServerHostAndPort.split(":");
                                    var port = 80;
                                    if (hpp.length == 2) {
                                        port = Integer.parseInt(hpp[1]);
                                    }
                                    if (config.isInterceptAllHttp()) {
                                        if (intSet.contains(port)) {
                                            InetSocketAddress res = null;
                                            try {
                                                res = new InetSocketAddress(
                                                        InetAddress.getByName("127.0.0.1"), port);
                                            } catch (UnknownHostException e) {
                                                return null;
                                            }
                                            return res;
                                        }
                                    }
                                    return null;
                                }

                            };
                        }
                    })
                    .withServerResolver(new HostResolver() {
                        @Override
                        public InetSocketAddress resolve(String address, int port) throws UnknownHostException {
                            var config = configuration.getConfiguration(Socks5Config.class).copy();
                            if (config.isInterceptAllHttp()) {
                                if (intSet.contains(port)) {
                                    var res = new InetSocketAddress(
                                            InetAddress.getByName("127.0.0.1"), port);
                                    return res;
                                }
                            }
                            var resolved = multiResolver.resolve(address);
                            if (resolved.isEmpty()) {
                                return null;
                            }
                            var res = resolved.get(0);
                            return new InetSocketAddress(
                                    InetAddress.getByName(res), port);
                        }
                    })
                    .start();
//            var proxyHttp = new HttpsProxyImpl(
//                    config.getHttpProxyPort(), false,
//                    loggerBuilder, multiResolver,
//                    config.isInterceptAllHttp());

            logger.info("Http/s proxy server LOADED, port: " + config.getHttpProxyPort());
            //proxyHttp.listen();
            while (running) {
                Sleeper.sleep(60000);
            }

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
