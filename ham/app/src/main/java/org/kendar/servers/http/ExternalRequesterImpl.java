package org.kendar.servers.http;

import com.networknt.schema.format.InetAddressValidator;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.message.BasicNameValuePair;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.MimeChecker;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.joining;

@Component
public class ExternalRequesterImpl implements ExternalRequester{
    public static final String BLOCK_RECURSION = "X-BLOCK-RECURSIVE";

    private static final HttpRequestRetryHandler requestRetryHandler =
            (exception, executionCount, context) -> executionCount != 1;
    private final Logger logger;
    private PoolingHttpClientConnectionManager connManager;
    private final RequestResponseBuilder requestResponseBuilder;
    private final DnsMultiResolver multiResolver;
    private final ConcurrentHashMap<String, AnsweringHandlerImpl.ResolvedDomain> domains = new ConcurrentHashMap<>();
    private SystemDefaultDnsResolver dnsResolver;

    public ExternalRequesterImpl(RequestResponseBuilder requestResponseBuilder,
                                 DnsMultiResolver multiResolver,
                                 LoggerBuilder loggerBuilder){
        this.logger = loggerBuilder.build(ExternalRequester.class);
        this.requestResponseBuilder = requestResponseBuilder;

        this.multiResolver = multiResolver;
    }
    @PostConstruct
    public void init() {
        this.dnsResolver =
                new SystemDefaultDnsResolver() {
                    @Override
                    public InetAddress[] resolve(final String host) throws UnknownHostException {
                        AnsweringHandlerImpl.ResolvedDomain descriptor;
                        var currentTime = Calendar.getInstance().getTimeInMillis();
                        List<String> hosts;
                        if (domains.containsKey(host)) {
                            descriptor = domains.get(host);
                            if ((descriptor.timestamp + 10 * 60 * 1000) < currentTime) {
                                domains.remove(host);
                                hosts = multiResolver.resolveRemote(host);
                                descriptor = new AnsweringHandlerImpl.ResolvedDomain();
                                descriptor.domains.addAll(hosts);
                                domains.put(host, descriptor);
                            }
                        } else {
                            hosts = multiResolver.resolveRemote(host);
                            descriptor = new AnsweringHandlerImpl.ResolvedDomain();
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
        this.connManager =
                new PoolingHttpClientConnectionManager(
                        // We're forced to create a SocketFactory Registry.  Passing null
                        //   doesn't force a default Registry, so we re-invent the wheel.
                        RegistryBuilder.<ConnectionSocketFactory>create()
                                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                                .build(),
                        dnsResolver // Our DnsResolver
                );

        this.connManager.setMaxTotal(100);
    }
    public PoolingHttpClientConnectionManager getConnectionManager(){
        return connManager;
    }
    public void callExternalSite(Request request, Response response)
            throws Exception {

        var resolved = multiResolver.resolveRemote(request.getHost());
        if(resolved.size()==0){
            if(!InetAddressValidator.getInstance().isValidInet4Address(request.getHost())) {
                response.setStatusCode(404);
                return;
            }
        }

        if(request.getHeader(BLOCK_RECURSION)!=null){
            response.setStatusCode(500);
            return;
        }


        CloseableHttpClient httpClient =
                HttpClientBuilder.create()
                        .setRetryHandler(requestRetryHandler)
                        .setDnsResolver(this.dnsResolver)
                        .setSchemePortResolver(httpHost -> {
                            if(request.getPort()>0) {
                                return request.getPort();
                            }
                            if(request.getProtocol().equalsIgnoreCase("https")) return 443;
                            return 80;
                        })
                        .setConnectionManager(connManager)
                        .disableConnectionState()
                        .disableRedirectHandling()
                        .build();

        HttpRequestBase fullRequest = null;
        try {
            String fullAddress = buildFullAddress(request);
            fullRequest = createFullRequest(request, fullAddress);
            fullRequest.addHeader(BLOCK_RECURSION, fullAddress);
            for (var header : request.getHeaders().entrySet()) {
                if (!header.getKey().equalsIgnoreCase("host")
                        && !header.getKey().equalsIgnoreCase("content-length")) {
                    fullRequest.addHeader(header.getKey(), header.getValue());
                }
            }
            fullRequest.addHeader("Host", request.getHost());
            if (request.isSoapRequest()) {
                HttpEntity entity = handleSoapRequest(request);
                ((HttpEntityEnclosingRequestBase) fullRequest).setEntity(entity);
            } else if (request.getPostParameters().size() > 0) {
                List<NameValuePair> form = new ArrayList<>();
                for (var par : request.getPostParameters().entrySet()) {
                    form.add(new BasicNameValuePair(par.getKey(), par.getValue()));
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);
                ((HttpEntityEnclosingRequestBase) fullRequest).setEntity(entity);
            } else if (requestResponseBuilder.isMultipart(request)) {
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                for (MultipartPart part : request.getMultipartData()) {
                    if (MimeChecker.isBinary(part.getContentType(), null)) {
                        builder.addBinaryBody(
                                part.getFieldName(),
                                part.getByteData(),
                                ContentType.create(part.getContentType()),
                                part.getFileName());
                    } else {
                        var type = part.getContentType();
                        if (type == null) {
                            type = "text/plain";
                        }
                        builder.addTextBody(
                                part.getFieldName(), part.getStringData(), ContentType.create(type));
                    }
                }
                HttpEntity entity = builder.build();
                ((HttpEntityEnclosingRequestBase) fullRequest).setEntity(entity);
            } else if (requestResponseBuilder.hasBody(request)) {
                HttpEntity entity;
                try {
                    String contentType = request.getHeader("content-type");
                    if(contentType.indexOf(";")>0){
                        var spl = contentType.split(";");
                        contentType = spl[0];
                    }
                    if (request.isBinaryRequest()) {
                        entity =
                                new ByteArrayEntity(
                                        request.getRequestBytes(), ContentType.create(contentType));

                    } else {
                        entity =
                                new StringEntity(
                                        request.getRequestText(), ContentType.create(contentType));
                    }
                }catch(Exception ex){
                    logger.debug("Error "+request.getHeader("content-type"),ex);
                    entity =
                            new StringEntity(
                                    request.getRequestText(), ContentType.create("application/octet-stream"));
                }
                ((HttpEntityEnclosingRequestBase) fullRequest).setEntity(entity);
            }

            HttpResponse httpResponse = null;
            try {
                httpResponse = httpClient.execute(fullRequest);
                requestResponseBuilder.fromHttpResponse(httpResponse, response);
            } catch (Exception ex) {
                response.setStatusCode(404);
                response.setResponseText(ex.getMessage());
                if(httpResponse!=null){
                    response.setStatusCode(httpResponse.getStatusLine().getStatusCode());
                    response.setResponseText(httpResponse.getStatusLine().getReasonPhrase()+" "+ex.getMessage());
                }
            }
        } finally {
            if (fullRequest != null) {
                fullRequest.releaseConnection();
            }
        }
    }


    private String buildFullAddress(Request request) {

        String port = "";
        if (request.getPort() != -1) {
            if (request.getPort() != 443 && request.getProtocol().equalsIgnoreCase("https")) {
                port = ":" + request.getPort();
            }

            if (request.getPort() != 80 && request.getProtocol().equalsIgnoreCase("http")) {
                port = ":" + request.getPort();
            }
        }
        return request.getProtocol()
                + "://"
                + request.getHost()
                + port
                + request.getPath()
                + buildFullQuery(request);
    }
    private String buildFullQuery(Request request) {
        if (request.getQuery().size() == 0) return "";
        return "?"
                + request.getQuery().entrySet().stream()
                .map(
                        e ->
                                e.getKey()
                                        + "="
                                        + java.net.URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)
                                        .replace(" ", "%20"))
                .collect(joining("&"));
    }

    private HttpEntity handleSoapRequest(Request request) {
        return null;
    }

    private HttpRequestBase createFullRequest(Request request, String stringAdress) throws Exception {
        var fullAddress = new URI(stringAdress);
        if (request.getMethod().equalsIgnoreCase("POST")) {
            return new HttpPost(fullAddress);
        } else if (request.getMethod().equalsIgnoreCase("PUT")) {
            return new HttpPut(fullAddress);
        } else if (request.getMethod().equalsIgnoreCase("PATCH")) {
            return new HttpPatch(fullAddress);
        } else if (request.getMethod().equalsIgnoreCase("GET")) {
            return new HttpGet(fullAddress);
        } else if (request.getMethod().equalsIgnoreCase("DELETE")) {
            return new HttpDelete(fullAddress);
        } else if (request.getMethod().equalsIgnoreCase("HEAD")) {
            return new HttpHead(fullAddress);
        } else if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            return new HttpOptions(fullAddress);
        } else if (request.getMethod().equalsIgnoreCase("TRACE")) {
            return new HttpTrace(fullAddress);
        } else {
            logger.error("MISSING METHOD " + request.getMethod() + " on " + fullAddress);
            throw new Exception("MISSING METHOD " + request.getMethod() + " on " + fullAddress);
        }
    }
}
