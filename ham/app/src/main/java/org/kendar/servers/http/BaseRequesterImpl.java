package org.kendar.servers.http;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
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
import org.apache.http.ssl.SSLContextBuilder;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.utils.ConnectionBuilder;
import org.kendar.utils.JsonSmile;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.MimeChecker;
import org.slf4j.Logger;

import javax.net.ssl.SSLContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseRequesterImpl implements BaseRequester{
    public static final String BLOCK_RECURSION = "X-BLOCK-RECURSIVE";

    private static final HttpRequestRetryHandler requestRetryHandler =
            (exception, executionCount, context) -> executionCount != 1;
    private final Logger logger;
    private PoolingHttpClientConnectionManager connManager;
    private final RequestResponseBuilder requestResponseBuilder;
    protected final DnsMultiResolver multiResolver;
    private final ConnectionBuilder connectionBuilder;
    protected final ConcurrentHashMap<String, ResolvedDomain> domains = new ConcurrentHashMap<>();
    private SystemDefaultDnsResolver dnsResolver;

    public BaseRequesterImpl(RequestResponseBuilder requestResponseBuilder,
                                 DnsMultiResolver multiResolver,
                                 LoggerBuilder loggerBuilder,
                             ConnectionBuilder connectionBuilder){
        this.logger = loggerBuilder.build(ExternalRequester.class);
        this.requestResponseBuilder = requestResponseBuilder;

        this.multiResolver = multiResolver;
        this.connectionBuilder = connectionBuilder;
    }

    public void callSite(Request request, Response response)
            throws Exception {



        if(request.getHeader(BLOCK_RECURSION)!=null){
            response.setStatusCode(500);
            return;
        }

        CloseableHttpClient httpClient;
        if(request.getHost().equalsIgnoreCase("127.0.0.1")||
                request.getHost().equalsIgnoreCase("localhost")){
            final SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, (x509CertChain, authType) -> true)
                    .build();
            httpClient = HttpClientBuilder.create().
                    setSSLContext(sslContext).
                    setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).
                    disableRedirectHandling().
                    build();
        }else {
            httpClient = connectionBuilder.buildClient(useRemoteDnsOnly(), true, request.getPort(), request.getProtocol());
        }

        HttpRequestBase fullRequest = null;
        try {
            String fullAddress = RequestUtils.buildFullAddress(request,true);
            fullRequest = createFullRequest(request, fullAddress);
            fullRequest.addHeader(BLOCK_RECURSION, fullAddress);
            if(request.getHeaders()!=null) {
                for (var header : request.getHeaders().entrySet()) {
                    if (!header.getKey().equalsIgnoreCase("host")
                            && !header.getKey().equalsIgnoreCase("content-length")) {
                        fullRequest.addHeader(header.getKey(), header.getValue());
                    }
                }
            }
            fullRequest.addHeader("Host", request.getHost());
            /*if (request.isSoapRequest()) {
                HttpEntity entity = handleSoapRequest(request);
                ((HttpEntityEnclosingRequestBase) fullRequest).setEntity(entity);
            } else */if (request.getPostParameters().size() > 0) {
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
                    if(JsonSmile.JSON_SMILE_MIME.equalsIgnoreCase(contentType)) {
                        entity =
                                new ByteArrayEntity(
                                        JsonSmile.jsonToSmile(request.getRequestText()), ContentType.create(contentType));
                    } else if (request.isBinaryRequest()) {
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

    protected abstract boolean useRemoteDnsOnly();


    private HttpRequestBase createFullRequest(Request request, String stringAdress) throws Exception {
        //var partialAddress= new URI(stringAdress).toString();
        var fullAddress = stringAdress;//"/"+String.join("/", Arrays.stream(partialAddress.split("/"))
                //.skip(3).collect(Collectors.toList()));

        //var fullAddress =
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
