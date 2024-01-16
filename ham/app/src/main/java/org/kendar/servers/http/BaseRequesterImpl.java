package org.kendar.servers.http;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.GzipCompressingEntity;
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
import org.kendar.utils.*;
import org.slf4j.Logger;

import javax.net.ssl.SSLContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("resource")
public abstract class BaseRequesterImpl implements BaseRequester {
    public static final String BLOCK_RECURSION = "X-BLOCK-RECURSIVE";

    private static final HttpRequestRetryHandler requestRetryHandler =
            (exception, executionCount, context) -> executionCount != 1;
    protected final DnsMultiResolver multiResolver;
    protected final ConcurrentHashMap<String, ResolvedDomain> domains = new ConcurrentHashMap<>();
    private final Logger logger;
    private final RequestResponseBuilder requestResponseBuilder;
    private final ConnectionBuilder connectionBuilder;
    private PoolingHttpClientConnectionManager connManager;
    private SystemDefaultDnsResolver dnsResolver;

    public BaseRequesterImpl(RequestResponseBuilder requestResponseBuilder,
                             DnsMultiResolver multiResolver,
                             LoggerBuilder loggerBuilder,
                             ConnectionBuilder connectionBuilder) {
        this.logger = loggerBuilder.build(ExternalRequester.class);
        this.requestResponseBuilder = requestResponseBuilder;

        this.multiResolver = multiResolver;
        this.connectionBuilder = connectionBuilder;
    }

    public void callSite(Request request, Response response)
            throws Exception {

        var contentEncoding = "";
        if (null != request.getHeader("content-encoding")) {
            contentEncoding = request.getHeader("content-encoding").toLowerCase(Locale.ROOT);
        }
        if (contentEncoding == null) contentEncoding = "";

        var brotli = contentEncoding.equalsIgnoreCase("br");
        var gzip = contentEncoding.equalsIgnoreCase("gzip");

        if (request.getHeader(BLOCK_RECURSION) != null) {
            response.setStatusCode(500);
            return;
        }

        CloseableHttpClient httpClient;
        if (request.getHost().equalsIgnoreCase("127.0.0.1") ||
                request.getHost().equalsIgnoreCase("localhost")) {
            final SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, (x509CertChain, authType) -> true)
                    .build();
            httpClient = HttpClientBuilder.create().
                    disableAutomaticRetries().
                    setSSLContext(sslContext).
                    setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).
                    disableRedirectHandling().
                    build();
        } else {
            httpClient = connectionBuilder.buildClient(useRemoteDnsOnly(), true, request.getPort(), request.getProtocol());
        }

        HttpRequestBase fullRequest = null;
        try {
            String fullAddress = RequestUtils.buildFullAddress(request, true);
            fullRequest = createFullRequest(request, fullAddress);
            fullRequest.addHeader(BLOCK_RECURSION, fullAddress);
            if (request.getHeaders() != null) {
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
            } else */
            if (request.getPostParameters().size() > 0) {
                List<NameValuePair> form = new ArrayList<>();
                for (var par : request.getPostParameters().entrySet()) {
                    form.add(new BasicNameValuePair(par.getKey(), par.getValue()));
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);

                if (gzip) {
                    ((HttpEntityEnclosingRequestBase) fullRequest).setEntity(new GzipCompressingEntity(entity));
                } else {
                    ((HttpEntityEnclosingRequestBase) fullRequest).setEntity(entity);
                }
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

                if (gzip) {
                    ((HttpEntityEnclosingRequestBase) fullRequest).setEntity(new GzipCompressingEntity(entity));
                } else {
                    ((HttpEntityEnclosingRequestBase) fullRequest).setEntity(entity);
                }
            } else if (requestResponseBuilder.hasBody(request)) {
                HttpEntity entity;
                try {
                    String contentType = request.getHeader(ConstantsHeader.CONTENT_TYPE);
                    if (contentType.indexOf(";") > 0) {
                        var spl = contentType.split(";");
                        contentType = spl[0];
                    }
                    if (ConstantsMime.JSON_SMILE.equalsIgnoreCase(contentType)) {
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
                } catch (Exception ex) {
                    logger.debug("Error " + request.getHeader(ConstantsHeader.CONTENT_TYPE), ex);
                    entity =
                            new StringEntity(
                                    request.getRequestText(), ContentType.create(ConstantsMime.STREAM));
                }
                if (gzip) {
                    ((HttpEntityEnclosingRequestBase) fullRequest).setEntity(new GzipCompressingEntity(entity));
                } else {
                    ((HttpEntityEnclosingRequestBase) fullRequest).setEntity(entity);
                }
            }

            HttpResponse httpResponse = null;
            try {
                httpResponse = httpClient.execute(fullRequest);
                requestResponseBuilder.fromHttpResponse(httpResponse, response);
            } catch (Exception ex) {
                response.setStatusCode(404);
                response.setResponseText(ex.getMessage());
                if (httpResponse != null) {
                    response.setStatusCode(httpResponse.getStatusLine().getStatusCode());
                    response.setResponseText(httpResponse.getStatusLine().getReasonPhrase() + " " + ex.getMessage());
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
        //.skip(3).collect(Collectors.toList()));

        //var fullAddress =
        if (request.getMethod().equalsIgnoreCase("POST")) {
            return new HttpPost(stringAdress);
        } else if (request.getMethod().equalsIgnoreCase("PUT")) {
            return new HttpPut(stringAdress);
        } else if (request.getMethod().equalsIgnoreCase("PATCH")) {
            return new HttpPatch(stringAdress);
        } else if (request.getMethod().equalsIgnoreCase("GET")) {
            return new HttpGet(stringAdress);
        } else if (request.getMethod().equalsIgnoreCase("DELETE")) {
            return new HttpDelete(stringAdress);
        } else if (request.getMethod().equalsIgnoreCase("HEAD")) {
            return new HttpHead(stringAdress);
        } else if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            return new HttpOptions(stringAdress);
        } else if (request.getMethod().equalsIgnoreCase("TRACE")) {
            return new HttpTrace(stringAdress);
        } else {
            logger.error("MISSING METHOD " + request.getMethod() + " on " + stringAdress);
            throw new Exception("MISSING METHOD " + request.getMethod() + " on " + stringAdress);
        }
    }
}
