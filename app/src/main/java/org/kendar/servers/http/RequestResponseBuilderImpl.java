package org.kendar.servers.http;

import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.kendar.utils.MimeChecker;
import org.kendar.utils.SimpleStringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class RequestResponseBuilderImpl implements RequestResponseBuilder {
    private final static String H_CONTENT_TYPE = "Content-Type";
    private final static String H_SOAPA_CTION = "SOAPAction";
    private final static String H_AUTHORIZATION = "Authorization";
    private final static String BASIC_AUTH_MARKER = "basic";
    private final static String BASIC_AUTH_SEPARATOR = ":";

    @Override
    public Request fromExchange(HttpExchange exchange, String protocol) throws IOException, FileUploadException {
        var result = new Request();
        result.setRemoteHost(exchange.getRemoteAddress().getHostName());
        result.setProtocol(protocol.toLowerCase(Locale.ROOT));
        result.setQuery(RequestUtils.queryToMap(exchange.getRequestURI().getRawQuery()));
        setupRequestHost(exchange, result);
        setupRequestPort(exchange, result);
        result.setPath(exchange.getRequestURI().getPath());
        result.setMethod(exchange.getRequestMethod().toUpperCase(Locale.ROOT));
        result.setHeaders(RequestUtils.headersToMap(exchange.getRequestHeaders()));
        var headerContentType = result.getHeader(H_CONTENT_TYPE);
        if(headerContentType == null){
            headerContentType = "application/octet-stream";
            result.addHeader(H_CONTENT_TYPE, headerContentType);
        }
        result.setSoapRequest(result.getHeader(H_SOAPA_CTION)!=null);
        setupAuthHeaders(result);

        result.setBinaryRequest(MimeChecker.isBinary(headerContentType,null));
        result.setStaticRequest(MimeChecker.isStatic(headerContentType,result.getPath()));
        setupOptionalBody(exchange, result);
        //result.sanitizedPath = RequestUtils.sanitizePath(result);
        return result;
    }

    @Override
    public boolean isMultipart(Request request) {
        var headerContentType = request.getHeader(H_CONTENT_TYPE);
        return headerContentType.toLowerCase(Locale.ROOT).startsWith("multipart");
    }

    @Override
    public boolean hasBody(Request request) {
        if(request.isBinaryRequest()){
            return request.getRequestBytes()!=null;
        }else{
            return request.getRequestText()!=null && !request.getRequestText().isEmpty();
        }
    }

    @Override
    public boolean hasBody(Response request) {
        if(request.isBinaryResponse()){
            return request.getResponseBytes()!=null;
        }else{
            return request.getResponseText()!=null && !request.getResponseText().isEmpty();
        }
    }

    @Override
    public Response fromHttpResponse(HttpResponse httpResponse, Response response) throws IOException {
        HttpEntity responseEntity = httpResponse.getEntity();
        if(responseEntity!=null) {
            InputStream in = responseEntity.getContent();

            String contentEncoding = null;
            if (null != responseEntity.getContentEncoding()) {
                contentEncoding = responseEntity.getContentEncoding().getValue().toLowerCase(Locale.ROOT);
            }
            if (responseEntity.getContentType()!=null && responseEntity.getContentType().getValue()!=null && MimeChecker.isBinary(responseEntity.getContentType().getValue(), contentEncoding)) {
                response.setResponseBytes(IOUtils.toByteArray(in));
                response.setBinaryResponse(true);
            } else {
                response.setResponseText(IOUtils.toString(in, StandardCharsets.UTF_8));
            }
        }else{
            response.setBinaryResponse(true);
            response.setResponseBytes(new byte[0]);
        }
        response.setStatusCode(httpResponse.getStatusLine().getStatusCode());
        for (var header:   httpResponse.getAllHeaders()) {
            if(header.getName().equalsIgnoreCase("transfer-encoding")) continue;
            response.addHeader(header.getName(),header.getValue());
        }
        return response;
    }

    private static void setupRequestHost(HttpExchange exchange, Request result) {
        result.setHost(exchange.getRequestURI().getHost());

        if(result.getHost()==null) {
            var data = exchange.getRequestHeaders().getFirst("Host").split(":", 2);
            if (data.length >= 1) {
                result.setHost(data[0]);
            }
        }
    }

    private static void setupRequestPort(HttpExchange exchange, Request result) {

        result.setPort(exchange.getRequestURI().getPort());
        if(result.getPort()<=0){
            var data = exchange.getRequestHeaders().getFirst("Host").split(":", 2);
            if (data.length == 2) {
                result.setPort(Integer.parseInt(data[1]));
            }
        }
        if(result.getPort()<=0) {
            var data = result.getHost().split(":", 2);
            if (data.length == 2) {
                result.setPort(Integer.parseInt(data[1]));
            }
        }
    }

    private static void setupOptionalBody(HttpExchange exchange, Request result) throws IOException, FileUploadException {
        var headerContentType = result.getHeader(H_CONTENT_TYPE);
        if(RequestUtils.isMethodWithBody(result)){
            //Calculate body
            if(headerContentType.toLowerCase(Locale.ROOT).startsWith("multipart")){
                Pattern rp = Pattern.compile("boundary", Pattern.CASE_INSENSITIVE);
                var boundary = SimpleStringUtils.splitByString("boundary=", headerContentType)[1];
                var data = IOUtils.toByteArray(exchange.getRequestBody());
                result.setMultipartData(RequestUtils.buildMultipart(data,boundary,result.getHeader("Content-type")));
            }else if(headerContentType.toLowerCase(Locale.ROOT).startsWith("application/x-www-form-urlencoded")){
                var requestText = IOUtils.toString(exchange.getRequestBody(),"UTF-8");
                result.setPostParameters(RequestUtils.queryToMap(requestText));
            }else {
                if (result.isBinaryRequest()) {
                    result.setRequestBytes(IOUtils.toByteArray(exchange.getRequestBody()));
                } else {
                    result.setRequestText(IOUtils.toString(exchange.getRequestBody(), "UTF-8"));
                }
            }
        }
    }

    private static void setupAuthHeaders(Request result) {
        var headerAuthorization = result.getHeader(H_AUTHORIZATION);
        if(headerAuthorization!=null &&
                headerAuthorization.toLowerCase(Locale.ROOT).startsWith(BASIC_AUTH_MARKER)){
            String base64Credentials = headerAuthorization.substring(BASIC_AUTH_MARKER.length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            // credentials = username:password
            final String[] values = credentials.split(BASIC_AUTH_SEPARATOR, 2);
            result.setBasicUsername(values[0]);
            result.setBasicPassword(values[1]);
        }
    }
}
