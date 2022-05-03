package org.kendar.ham;

import org.xbill.DNS.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class HamBuilder implements HamInternalBuilder {

    String host;
    Integer port;
    String protocol;
    private String dnsServer;
    private int dnsPort;
    private int proxyPort;
    private String proxyIp;

    private HamBuilder() {
    }

    public static HamBasicBuilder newHam(String host) {
        var result = new HamBuilder();
        result.host = host;
        result.port = null;
        result.protocol = "http";
        result.dnsServer = "127.0.0.1";
        result.dnsPort = 53;
        return result;
    }

    private static HashMap<String, Function<HamInternalBuilder, Object>> pluginBuilders;

    public static void register(String index, Function<HamInternalBuilder, Object> clazz) {
        if (pluginBuilders == null) pluginBuilders = new HashMap<>();
        pluginBuilders.put(index.toLowerCase(Locale.ROOT), clazz);
    }

    public HamBasicBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    @Override
    public HamBasicBuilder withDns(String ip, int port) {
        this.dnsServer = ip;
        this.dnsPort = port;
        return this;
    }

    @Override
    public HamBasicBuilder withSocksProxy(String ip, int port) {
        this.proxyIp = ip;
        this.proxyPort = port;
        return this;
    }

    @Override
    public HamBasicBuilder withDns(String ip) {
        return withDns(ip, 53);
    }

    public HamBasicBuilder withHttps() {
        this.protocol = "https";
        return this;
    }

    public DnsBuilder dns() {
        return new DnsBuilderImpl(this);
    }

    public HamRequestBuilder newRequest() {
        var result = HamRequestBuilder.newRequest(protocol, host);
        if (port != null) result.withPort(port);
        return result;
    }

    ObjectMapper mapper = new ObjectMapper();


    public Response expectCode(Response response, int code, Supplier<String> getExceptionMessage) throws HamException {
        if (response.getStatusCode() != code) {
            throw new HamException(getExceptionMessage.get());
        }
        return response;
    }

    public Response expectCode(Response response, int code, String getExceptionMessage) throws HamException {
        if (response.getStatusCode() != code) {
            throw new HamException(getExceptionMessage);
        }
        return response;
    }

    public <T> T callJson(Request request, Class<T> clazz) throws HamException {
        try {
            return (T) mapper.readValue(request.getRequestText(), clazz);
        } catch (JsonProcessingException e) {
            throw new HamException(e);
        }
    }

    public static String updateMethod(Optional val) {
        return val.isPresent() ? "PUT" : "POST";
    }

    public static String pathId(String path, Optional val, Supplier<String> idSupplier) {
        return path + (val.isPresent() ? "/" + idSupplier.get() : "");
    }

    public static void queryId(Request request, Optional val, String name, Supplier<String> idSupplier) {
        if (val.isPresent()) {
            request.addQuery(name, idSupplier.get());
        }
    }

    public CertificatesBuilder certificates() {
        return new CertificatesBuilderImpl(this);
    }

    public ProxyBuilder proxyes() {
        return new ProxyBuilderImpl(this);
    }

    public Object pluginBuilder(String name) {
        return pluginBuilders.get(name.toLowerCase(Locale.ROOT)).apply(this);
    }


    public <T> List<T> callJsonList(Request request, Class<T> clazz) throws HamException {
        try {
            var response = call(request);
            return (List<T>) mapper.readValue(response.getResponseText(), new TypeReference<List<T>>() {
            });
        } catch (JsonProcessingException e) {
            throw new HamException(e);
        }
    }

    static class MyConnectionSocketFactory extends SSLConnectionSocketFactory {

        public MyConnectionSocketFactory(final SSLContext sslContext) {
            super(sslContext);
        }

        @Override
        public Socket createSocket(final HttpContext context) throws IOException {
            InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
            return new Socket(proxy);
        }

    }



    private CloseableHttpResponse execute(HttpUriRequest request) throws IOException {
        var dnsServer = this.dnsServer;
        var dnsPort = this.dnsPort;
        DnsResolver dnsResolver = new SystemDefaultDnsResolver() {
            @Override
            public InetAddress[] resolve(final String requestedDomain) throws UnknownHostException {
                try {

                    var resolver = new SimpleResolver(dnsServer);
                    resolver.setPort(dnsPort);
                    var resolvers = new ArrayList<Resolver>();
                    resolvers.add(resolver);
                    ExtendedResolver extendedResolver = new ExtendedResolver(resolvers);
                    extendedResolver.setTimeout(Duration.ofSeconds(1));
                    extendedResolver.setRetries(0);
                    Lookup lookup = new Lookup(requestedDomain, Type.A);
                    lookup.setResolver(extendedResolver);
                    lookup.setCache(null);
                    lookup.setHostsFileParser(null);
                    var records = lookup.run();
                    if(records!=null){
                        for (org.xbill.DNS.Record record:records) {
                            String realip = ((ARecord) records[0]).getAddress().getHostAddress();
                            return new InetAddress[]{InetAddress.getByName(realip)};
                        }
                    }
                    return new InetAddress[]{};
                }catch (Exception ex){
                    return new InetAddress[]{};
                }
            }
        };
        if(this.proxyIp!=null) {
            Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", new MyConnectionSocketFactory(SSLContexts.createSystemDefault()))
                    .build();
            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg,dnsResolver);
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setDnsResolver(dnsResolver)
                    .setConnectionManager(cm)
                    .build();

            InetSocketAddress socksaddr = new InetSocketAddress(this.proxyIp, this.proxyPort);
            HttpClientContext context = HttpClientContext.create();
            context.setAttribute("socks.address", socksaddr);

            return httpclient.execute(request, context);
        }else{
            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setDnsResolver(dnsResolver)
                    .setConnectionManager(cm)
                    .build();

            return httpClient.execute(request);
        }
    }

    public Response call(Request request) throws HamException {
        try {
            HttpPost httpPost = new HttpPost(getHamAddress() + "/api/remote/execute");

            httpPost.addHeader("content-type", "application/json");
            httpPost.setEntity(new StringEntity(mapper.writeValueAsString(request)));

            CloseableHttpResponse clientResponse = execute(httpPost);
            String json = IOUtils.toString(clientResponse.getEntity().getContent(), StandardCharsets.UTF_8);
            return mapper.readValue(json, Response.class);
        } catch (Exception ex) {
            throw new HamException(ex);
        }
    }

    private String getHamAddress() {
        if (port == null) {
            return protocol + "://" + host;
        }
        return protocol + "://" + host + ":" + port.toString();
    }
}
