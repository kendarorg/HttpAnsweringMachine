package org.kendar.utils;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.ssl.SSLContextBuilder;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.servers.http.ResolvedDomain;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConnectionBuilderImpl implements ConnectionBuilder{
    private static final HttpRequestRetryHandler requestRetryHandler =
            (exception, executionCount, context) -> executionCount != 3;
    private final DnsMultiResolver multiResolver;
    protected final ConcurrentHashMap<String, ResolvedDomain> domains = new ConcurrentHashMap<>();
    private final Logger logger;
    private SystemDefaultDnsResolver remoteDnsResolver;
    private SystemDefaultDnsResolver fullDnsResolver;

    public ConnectionBuilderImpl(DnsMultiResolver multiResolver,
                                 LoggerBuilder loggerBuilder){
        this.multiResolver = multiResolver;
        logger = loggerBuilder.build(ConnectionBuilder.class);
    }

    private SystemDefaultDnsResolver buildFullResolver() {
        return  new SystemDefaultDnsResolver() {
            @Override
            public InetAddress[] resolve(final String host) throws UnknownHostException {
                var result = multiResolver.resolve(host);
                if(!result.isEmpty()) {
                    return new InetAddress[]{InetAddress.getByName(result.get(0))};
                }
                return new InetAddress[]{};
            }
        };
    }

    private SystemDefaultDnsResolver buildRemoteResolver() {
        return  new SystemDefaultDnsResolver() {
            @Override
            public InetAddress[] resolve(final String host) throws UnknownHostException {
                ResolvedDomain descriptor;
                var currentTime = Calendar.getInstance().getTimeInMillis();
                List<String> hosts;
                if (domains.containsKey(host)) {
                    descriptor = domains.get(host);
                    if ((descriptor.timestamp + 10 * 60 * 1000) < currentTime) {
                        domains.remove(host);
                        hosts = multiResolver.resolveRemote(host);
                        descriptor = new ResolvedDomain();
                        descriptor.domains.addAll(hosts);
                        domains.put(host, descriptor);
                    }
                } else {
                    hosts = multiResolver.resolveRemote(host);
                    descriptor = new ResolvedDomain();
                    descriptor.domains.addAll(hosts);
                    domains.put(host, descriptor);
                }
                hosts = new ArrayList<>(descriptor.domains);
                var address = new InetAddress[hosts.size()];
                for (int i = 0; i < hosts.size(); i++) {
                    address[i] = InetAddress.getByName(hosts.get(i));
                }
                return address;
            }
        };
    }

    private Registry<ConnectionSocketFactory> buildSslUncheckedRegistry() {
        SSLContextBuilder contextBuilder = new SSLContextBuilder();
        try {
            contextBuilder.loadTrustMaterial(null, (TrustStrategy) (chain, authType) -> true);
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(contextBuilder.build(),
                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            //SSLConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSocketFactory()).build()

            RegistryBuilder<ConnectionSocketFactory> builder = RegistryBuilder.create();
            return builder
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslsf).build();

        } catch (NoSuchAlgorithmException|KeyStoreException|KeyManagementException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Registry<ConnectionSocketFactory> buildDefaultRegistry() {
        return RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();
    }

    @PostConstruct
    public void init(){
        this.remoteDnsResolver= buildRemoteResolver();
        this.fullDnsResolver = buildFullResolver();
        Registry<ConnectionSocketFactory> defaultRegistry = buildDefaultRegistry();
        Registry<ConnectionSocketFactory> sslUncheckedRegistry = buildSslUncheckedRegistry();
    }

    public HttpClientConnectionManager getConnectionManger(boolean remoteDns){
        return getConnectionManger(remoteDns,true);
    }

    public HttpClientConnectionManager getConnectionManger(boolean remoteDns, boolean checkSsl){
        return new PoolingHttpClientConnectionManager();
    }

    @Override
    public CloseableHttpClient buildClient(boolean remoteDns, boolean checkSsl, int port,String protocol) {
        var dnsResolver = remoteDns?this.remoteDnsResolver:this.fullDnsResolver;
        SSLContext sslContext = null;
        try {
            sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, (x509CertChain, authType) -> true)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return HttpClientBuilder.create()
                .setSSLContext(sslContext).
                setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).
                        setDnsResolver(dnsResolver)
                .setConnectionManagerShared(true)
                .disableRedirectHandling()
                .build();
    }
}
