package org.kendar.servers.http;

import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.brotli.dec.BrotliInputStream;
import org.kendar.utils.*;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

@Component
public class RequestResponseBuilderImpl implements RequestResponseBuilder {

    private static final String H_SOAP_ACTION = "SOAPAction";
    private static final String H_AUTHORIZATION = "Authorization";
    private static final String BASIC_AUTH_MARKER = "basic";
    private static final String BASIC_AUTH_SEPARATOR = ":";
    private static Logger logger;
    public RequestResponseBuilderImpl(LoggerBuilder loggerBuilder) {
        this.logger = loggerBuilder.build(RequestResponseBuilderImpl.class);
    }

    private static void setupRequestHost(HttpExchange exchange, Request result) {
        result.setHost(exchange.getRequestURI().getHost());

        if (result.getHost() == null) {
            var data = exchange.getRequestHeaders().getFirst("Host").split(":", 2);
            if (data.length >= 1) {
                result.setHost(data[0]);
            }
        }
    }

    private static void setupRequestPort(HttpExchange exchange, Request result) {

        result.setPort(exchange.getRequestURI().getPort());
        if (result.getPort() <= 0) {
            var data = exchange.getRequestHeaders().getFirst("Host").split(":", 2);
            if (data.length == 2) {
                result.setPort(Integer.parseInt(data[1]));
            }
        }
        if (result.getPort() <= 0) {
            var data = result.getHost().split(":", 2);
            if (data.length == 2) {
                result.setPort(Integer.parseInt(data[1]));
            }
        }
    }

    private static void setupOptionalBody(HttpExchange exchange, Request result)
            throws IOException, FileUploadException {
        var headerContentType = result.getHeader(ConstantsHeader.CONTENT_TYPE);

        if (RequestUtils.isMethodWithBody(result)) {

            var data = IOUtils.toByteArray(exchange.getRequestBody());
            var contentEncoding = "";
            if (null != result.getHeader("content-encoding")) {
                contentEncoding = result.getHeader("content-encoding").toLowerCase(Locale.ROOT);
            }
            if (contentEncoding == null) contentEncoding = "";

            var brotli = contentEncoding.equalsIgnoreCase("br");
            var gzip = contentEncoding.equalsIgnoreCase("gzip");
            if (gzip) {
                InputStream bodyStream = new GZIPInputStream(new ByteArrayInputStream(data));
                data = IOUtils.toByteArray(bodyStream);
            } else if (brotli) {
                InputStream bodyStream = new BrotliInputStream(new ByteArrayInputStream(data));
                data = IOUtils.toByteArray(bodyStream);
            }

            // Calculate body
            if (headerContentType != null && headerContentType.toLowerCase(Locale.ROOT).startsWith("multipart")) {
                Pattern rp = Pattern.compile("boundary", Pattern.CASE_INSENSITIVE);
                var boundary = SimpleStringUtils.splitByString("boundary=", headerContentType)[1];
                result.setMultipartData(
                        RequestUtils.buildMultipart(data, boundary, result.getHeader(ConstantsHeader.CONTENT_TYPE)));
            } else if (headerContentType != null && headerContentType
                    .toLowerCase(Locale.ROOT)
                    .startsWith("application/x-www-form-urlencoded")) {
                var requestText = new String(data, StandardCharsets.UTF_8);
                result.setPostParameters(RequestUtils.queryToMap(requestText));
            } else if (headerContentType != null && headerContentType
                    .toLowerCase(Locale.ROOT)
                    .startsWith(ConstantsMime.JSON_SMILE)) {
                var requestText = JsonSmile.smileToJSON(data).toPrettyString();
                result.setRequestText(requestText);
            } else {
                if (result.isBinaryRequest()) {
                    result.setRequestBytes(data);
                } else {
                    result.setRequestText(new String(data, StandardCharsets.UTF_8));
                }
            }
        }
    }

    private static void setupAuthHeaders(Request result) {
        var headerAuthorization = result.getHeader(H_AUTHORIZATION);
        if (headerAuthorization != null
                && headerAuthorization.toLowerCase(Locale.ROOT).startsWith(BASIC_AUTH_MARKER)) {
            String base64Credentials = headerAuthorization.substring(BASIC_AUTH_MARKER.length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            // credentials = username:password
            final String[] values = credentials.split(BASIC_AUTH_SEPARATOR, 2);
            result.setBasicUsername(values[0]);
            result.setBasicPassword(values[1]);
        }
    }

    @Override
    public Request fromExchange(HttpExchange exchange, String protocol)
            throws IOException, FileUploadException {
        var result = new Request();
        result.setRemoteHost(exchange.getRemoteAddress().getAddress().getHostAddress());
        result.setProtocol(protocol.toLowerCase(Locale.ROOT));

        result.setQuery(RequestUtils.queryToMap(exchange.getRequestURI().getRawQuery()));


        setupRequestHost(exchange, result);
        setupRequestPort(exchange, result);
        result.setPath(exchange.getRequestURI().getRawPath());
        result.setMethod(exchange.getRequestMethod().toUpperCase(Locale.ROOT));
        result.setHeaders(RequestUtils.headersToMap(exchange.getRequestHeaders()));
        var headerContentType = result.getHeader(ConstantsHeader.CONTENT_TYPE);
        if (headerContentType == null) {
            //result.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.STREAM);
        }
        result.setSoapRequest(result.getHeader(H_SOAP_ACTION) != null);
        setupAuthHeaders(result);

        result.setBinaryRequest(MimeChecker.isBinary(headerContentType, ""));
        result.setStaticRequest(MimeChecker.isStatic(headerContentType, result.getPath())
                // && result.getQuery().size()==0
        );
        setupOptionalBody(exchange, result);
        // result.sanitizedPath = RequestUtils.sanitizePath(result);
        return result;
    }

    @Override
    public boolean isMultipart(Request request) {
        var headerContentType = request.getHeader(ConstantsHeader.CONTENT_TYPE);
        if (headerContentType == null) return false;
        return headerContentType.toLowerCase(Locale.ROOT).startsWith("multipart");
    }

    @Override
    public boolean hasBody(Request request) {
        if (request.isBinaryRequest()) {
            return request.getRequestBytes() != null;
        } else {
            return request.getRequestText() != null && !request.getRequestText().isEmpty();
        }
    }

    @Override
    public boolean hasBody(Response request) {
        if (request.isBinaryResponse()) {
            return request.getResponseBytes() != null;
        } else {
            return request.getResponseText() != null && !request.getResponseText().isEmpty();
        }
    }

    @Override
    public void fromHttpResponse(HttpResponse httpResponse, Response response)
            throws IOException {
        HttpEntity responseEntity = httpResponse.getEntity();

        var brotli = false;
        String contentEncoding = "";
        if (responseEntity != null) {
            InputStream in = responseEntity.getContent();

            if (null != responseEntity.getContentEncoding()) {
                contentEncoding = responseEntity.getContentEncoding().getValue().toLowerCase(Locale.ROOT);
            }
            if (contentEncoding == null) contentEncoding = "";


            brotli = contentEncoding.equalsIgnoreCase("br");
            if (responseEntity.getContentType() != null
                    && responseEntity.getContentType().getValue() != null
                    && MimeChecker.isBinary(responseEntity.getContentType().getValue(), contentEncoding)) {

                if (brotli) {
                    response.setResponseBytes(IOUtils.toByteArray(new BrotliInputStream(in)));
                    response.removeHeader("content-encoding");
                } else {
                    response.setResponseBytes(IOUtils.toByteArray(in));
                }
                response.setBinaryResponse(true);
            } else {
                String responseText = null;
                if (brotli) {
                    responseText = IOUtils.toString(new BrotliInputStream(in), StandardCharsets.UTF_8);
                    response.removeHeader("content-encoding");
                } else if (responseEntity.getContentType() != null && responseEntity.getContentType().getValue().equalsIgnoreCase(ConstantsMime.JSON_SMILE)) {
                    responseText = JsonSmile.smileToJSON(IOUtils.toByteArray(in)).toPrettyString();
                } else {
                    responseText = IOUtils.toString(in, StandardCharsets.UTF_8);
                }
                response.setResponseText(responseText);
            }
        } else {
            response.setBinaryResponse(true);
            response.setResponseBytes(new byte[0]);
        }
        response.setStatusCode(httpResponse.getStatusLine().getStatusCode());
        for (var header : httpResponse.getAllHeaders()) {
            if (header.getName().equalsIgnoreCase("transfer-encoding")) continue;
            response.addHeader(header.getName(), header.getValue());
        }
        if (brotli) {
            response.removeHeader("content-encoding");
        }
    }
}
