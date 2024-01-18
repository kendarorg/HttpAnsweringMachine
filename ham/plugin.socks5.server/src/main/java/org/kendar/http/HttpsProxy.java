package org.kendar.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.kendar.events.EventQueue;
import org.kendar.events.events.SSLChangedEvent;
import org.kendar.servers.AnsweringServer;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.HttpWebServerConfig;
import org.kendar.servers.config.HttpsWebServerConfig;
import org.kendar.servers.config.SSLConfig;
import org.kendar.servers.config.SSLDomain;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class HttpsProxy implements AnsweringServer {
    private final Logger logger;
    private final LoggerBuilder loggerBuilder;
    private final JsonConfiguration configuration;
    private final EventQueue eventQueue;
    private final DnsMultiResolver multiResolver;
    private boolean running = false;
    private HttpProxyServer server;

    public HttpsProxy(DnsMultiResolver multiResolver, LoggerBuilder loggerBuilder,
                      JsonConfiguration configuration, EventQueue eventQueue) {

        this.multiResolver = multiResolver;
        this.logger = loggerBuilder.build(HttpsProxy.class);
        this.loggerBuilder = loggerBuilder;
        this.configuration = configuration;
        this.eventQueue = eventQueue;
    }

    @Override
    public void run() {
        if (running) return;
        var httpsResolved = new ConcurrentHashMap<String,String>();
        var config = configuration.getConfiguration(Socks5Config.class).copy();
        var httpConfig = configuration.getConfiguration(HttpWebServerConfig.class).copy();
        var httpsConfig = configuration.getConfiguration(HttpsWebServerConfig.class).copy();
        var spl = httpConfig.getPort().split(";");
        var spls = httpsConfig.getPort().split(";");
        var intSet = new HashSet<Integer>();
        var intSets = new HashSet<Integer>();
        for (var sp : spl) {
            intSet.add(Integer.parseInt(sp));
        }
        for (var sp : spls) {
            intSets.add(Integer.parseInt(sp));
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

                                    return null;// doResolve(resolvingServerHostAndPort,config,intSet,intSets,httpsResolved);

                                }

                                @Override
                                public HttpResponse clientToProxyRequest(HttpObject httpObject) {
                                    if(httpObject instanceof DefaultHttpRequest){
                                        var r = (DefaultHttpRequest)httpObject;
                                        if(r.method()== HttpMethod.CONNECT){
                                            doResolve(r.uri(),config,intSet,intSets,httpsResolved);

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
                            return doResolve(address+":"+port,config,intSet,intSets,httpsResolved);
                        };
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

    private InetSocketAddress doResolve(String resolvingServerHostAndPort,
                                        Socks5Config config, HashSet<Integer> intSet,
                                        HashSet<Integer> intSets,
                                        ConcurrentHashMap<String, String> httpsResolved) {
        var hpp = resolvingServerHostAndPort.split(":");
        var host = hpp[0];
        var port = 80;
        if (hpp.length == 2) {
            port = Integer.parseInt(hpp[1]);
        }
        if (config.isInterceptAllHttp()) {
            if(intSets.contains(port)){
                var namesp =host.split("\\.");
                if(namesp.length>2){
                    tryResolving(httpsResolved,  "*." + namesp[namesp.length - 2] + "." + namesp[namesp.length - 1]);
                }else if(namesp.length==2){
                    tryResolving(httpsResolved,  "*." + namesp[namesp.length - 2] + "." + namesp[namesp.length - 1]);
                    tryResolving(httpsResolved,  namesp[namesp.length - 2] + "." + namesp[namesp.length - 1]);


                }
            }
            if(intSet.contains(port)||intSets.contains(port)){
                try {
                    return new InetSocketAddress(
                            InetAddress.getByName("127.0.0.1"), port);
                } catch (UnknownHostException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private void tryResolving(ConcurrentHashMap<String, String> httpsResolved, String host) {
        var addHttps = httpsResolved.computeIfAbsent(host, name -> {
            var cloned = configuration.getConfiguration(SSLConfig.class).copy();

            System.out.println("CERTICATE "+name);
            ArrayList<SSLDomain> newList = new ArrayList<>();
            var newDomain = new SSLDomain();
            newDomain.setId(UUID.randomUUID().toString());
            newDomain.setAddress((String) name);
            newList.add(newDomain);
            for (var item : cloned.getDomains()) {
                if (item.getAddress().equalsIgnoreCase(name)) {
                    continue;
                }
                newList.add(item);
            }
            cloned.setDomains(newList);
            configuration.setConfiguration(cloned);
            eventQueue.handle(new SSLChangedEvent());

            Sleeper.sleep(100);
            return name;
        });
    }


    @Override
    public boolean shouldRun() {
        var localConfig = configuration.getConfiguration(Socks5Config.class);
        return localConfig.isActive() && !running;
    }
}
