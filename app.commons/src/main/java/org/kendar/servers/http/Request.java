package org.kendar.servers.http;

import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.io.IOUtils;
import org.kendar.utils.MimeChecker;
import org.kendar.utils.SimpleStringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class Request {
    private final static String H_CONTENT_TYPE = "Content-Type";
    private final static String H_SOAPA_CTION = "SOAPAction";
    private final static String H_AUTHORIZATION = "Authorization";
    private final static String BASIC_AUTH_MARKER = "basic";
    private final static String BASIC_AUTH_SEPARATOR = ":";
    private String remoteHost;


    public static void fromSerializable(Request result,SerializableRequest request) {
        result.method = request.getMethod();
        result.binaryRequest = request.binaryRequest;
        if(request.isBinaryRequest()){
            result.requestBytes = request.getRequestBytes();

        }else {
            result.requestText = request.getRequestText();
        }
        result.headers = request.getHeaders();
        result.protocol = request.getProtocol();
        result.soapRequest = request.isSoapRequest();
        result.basicPassword = request.getBasicPassword();
        result.basicUsername = request.getBasicUsername();
        result.multipartData = request.getMultipartData();
        result.staticRequest = request.isStaticRequest();
        result.host = request.getHost();
        result.path = request.getPath();
        result.postParameters = request.getPostParameters();
        result.port = request.getPort();
        result.query = request.getQuery();
    }

    public static SerializableRequest toSerializable(Request request){
        var result = new SerializableRequest();
        result.method = request.getMethod();
        if(request.isBinaryRequest()){
            result.requestBytes = (byte[])request.getRequest();
        }else {
            result.requestText = (String)request.getRequest();
        }
        result.headers = request.getHeaders();
        result.protocol = request.getProtocol();
        result.soapRequest = request.isSoapRequest();
        result.basicPassword = request.getBasicPassword();
        result.basicUsername = request.getBasicUsername();
        result.multipartData = request.getMultipartData();
        result.staticRequest = request.isStaticRequest();
        result.host = request.getHost();
        result.path = request.getPath();
        result.postParameters = request.getPostParameters();
        result.port = request.getPort();
        result.query = request.getQuery();
        return result;

    }

    public static Request fromExchange(HttpExchange exchange, String protocol, int forwardPort) throws IOException {
        var result = new Request();
        result.remoteHost = exchange.getRemoteAddress().getHostName();
        result.protocol = protocol.toLowerCase(Locale.ROOT);
        result.query = RequestUtils.queryToMap(exchange.getRequestURI().getRawQuery());
        setupRequestHost(exchange, result);
        setupRequestPort(exchange, forwardPort, result);
        result.path = exchange.getRequestURI().getPath();
        result.method = exchange.getRequestMethod().toUpperCase(Locale.ROOT);
        result.headers = RequestUtils.headersToMap(exchange.getRequestHeaders());
        result.headerContentType = result.getHeader(H_CONTENT_TYPE);
        if(result.headerContentType == null){
            result.headerContentType = "application/octet-stream";
        }
        result.headerSoapAction = result.getHeader(H_SOAPA_CTION);
        result.soapRequest = result.headerSoapAction !=null;
        setupAuthHeaders(result);

        result.binaryRequest  = MimeChecker.isBinary(result.getHeaderContentType(),null);
        result.staticRequest = MimeChecker.isStatic(result.getHeaderContentType(),result.path);
        setupOptionalBody(exchange, result);
        result.sanitizedPath = RequestUtils.sanitizePath(result);
        return result;
    }

    private static void setupOptionalBody(HttpExchange exchange, Request result) throws IOException {
        if(RequestUtils.isMethodWithBody(result)){
            //Calculate body
            if(result.headerContentType.toLowerCase(Locale.ROOT).startsWith("multipart")){
                Pattern rp = Pattern.compile("boundary", Pattern.CASE_INSENSITIVE);
                var boundary = SimpleStringUtils.splitByString("boundary=", result.headerContentType)[1];
                var text = IOUtils.toString(exchange.getRequestBody(),"UTF-8");
                var splittedText = text.split("\r\n");
                result.multipartData = RequestUtils.buildMultipart(splittedText,boundary);
                result.multipart = true;
            }else if(result.headerContentType.toLowerCase(Locale.ROOT).startsWith("application/x-www-form-urlencoded")){
                var requestText = IOUtils.toString(exchange.getRequestBody(),"UTF-8");
                result.postParameters = RequestUtils.queryToMap(requestText);
            }else {
                if (result.binaryRequest) {
                    result.requestBytes = IOUtils.toByteArray(exchange.getRequestBody());
                } else {
                    result.requestText = IOUtils.toString(exchange.getRequestBody(), "UTF-8");
                }
            }
        }
    }

    private static void setupAuthHeaders(Request result) {
        result.headerAuthorization = result.getHeader(H_AUTHORIZATION);
        if(result.headerAuthorization!=null &&
                result.headerAuthorization.toLowerCase(Locale.ROOT).startsWith(BASIC_AUTH_MARKER)){
            String base64Credentials = result.headerAuthorization.substring(BASIC_AUTH_MARKER.length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            // credentials = username:password
            final String[] values = credentials.split(BASIC_AUTH_SEPARATOR, 2);
            result.basicUsername = values[0];
            result.basicPassword = values[1];
        }
    }

    private static void setupRequestHost(HttpExchange exchange, Request result) {
        result.host = exchange.getRequestURI().getHost();

        if(result.host==null) {
            var data = exchange.getRequestHeaders().getFirst("Host").split(":", 2);
            if (data.length >= 1) {
                result.host = data[0];
            }
        }
    }

    private static void setupRequestPort(HttpExchange exchange, int forwardPort, Request result) {
        result.port = forwardPort;
        if(forwardPort == -1) {
            result.port = exchange.getRequestURI().getPort();
            if(result.port<=0){
                var data = exchange.getRequestHeaders().getFirst("Host").split(":", 2);
                if (data.length == 2) {
                    result.port = Integer.parseInt(data[1]);
                }
            }
            if(result.port<=0) {
                var data = result.host.split(":", 2);
                if (data.length == 2) {
                    result.port = Integer.parseInt(data[1]);
                }
            }
        }
    }

    private long ms = Calendar.getInstance().getTimeInMillis();
    private Map<String,String> headers = new HashMap<>();
    private Map<String,String> query = new HashMap<>();
    private Map<String,String> postParameters = new HashMap<>();
    private Map<String,String> pathParameters = new HashMap<>();
    private String host;
    private String path;
    private int port;
    private String protocol;
    private boolean staticRequest;
    private boolean binaryRequest;
    private byte[] requestBytes = null;
    private String requestText = null;
    private boolean soapRequest;
    private String headerContentType;
    private String headerSoapAction;
    private String headerAuthorization;
    private String basicUsername;
    private String basicPassword;
    private String method;
    private String sanitizedPath;
    private boolean multipart;
    private List<MultipartPart> multipartData = new ArrayList<>();

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getHeader(String index) {
        return RequestUtils.getFromMap(this.headers,index);
    }

    public void setHeader(String index,String data){
        this.headers.put(index,data);
    }

    public String removeHeader(String index){
        return RequestUtils.removeFromMap(this.headers,index);
    }

    public void setQuery(String index,String data){
        this.query.put(index,data);
    }

    public Map<String, String> getQuery() {
        return query;
    }

    public String removeQuery(String index){
        return RequestUtils.removeFromMap(this.query,index);
    }

    public void setQuery(Map<String, String> query) {
        this.query = query;
    }

    public String getQuery(String index) {
        return RequestUtils.getFromMap(this.query,index);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public boolean isStaticRequest() {
        return staticRequest;
    }

    public void setStaticRequest(boolean staticRequest) {
        this.staticRequest = staticRequest;
    }

    public boolean isBinaryRequest() {
        return binaryRequest;
    }

    public void setBinaryRequest(boolean binaryRequest) {
        this.binaryRequest = binaryRequest;
    }

    public void setRequest(byte[] requestBytes) {
        this.requestBytes = requestBytes;
    }

    public void setRequest(String requestText) {
        this.requestText = requestText;
    }

    public Object getRequest(){
        if(this.isBinaryRequest()){
            return this.requestBytes;
        }else{
            return this.requestText;
        }
    }

    public boolean isSoapRequest() {
        return soapRequest;
    }

    public void setSoapRequest(boolean soapRequest) {
        this.soapRequest = soapRequest;
    }

    public String getHeaderContentType() {
        return headerContentType;
    }

    public void setHeaderContentType(String headerContentType) {
        this.headerContentType = headerContentType;
    }

    public String getHeaderSoapAction() {
        return headerSoapAction;
    }

    public void setHeaderSoapAction(String headerSoapAction) {
        this.headerSoapAction = headerSoapAction;
    }

    public String getHeaderAuthorization() {
        return headerAuthorization;
    }

    public void setHeaderAuthorization(String headerAuthorization) {
        this.headerAuthorization = headerAuthorization;
    }

    public String getBasicUsername() {
        return basicUsername;
    }

    public void setBasicUsername(String basicUsername) {
        this.basicUsername = basicUsername;
    }

    public String getBasicPassword() {
        return basicPassword;
    }

    public void setBasicPassword(String basicPassword) {
        this.basicPassword = basicPassword;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getSanitizedPath() {
        return sanitizedPath;
    }

    public void setSanitizedPath(String sanitizedPath) {
        this.sanitizedPath = sanitizedPath;
    }

    public boolean isMultipart() {
        return multipart;
    }

    public void setMultipart(boolean multipart) {
        this.multipart = multipart;
    }


    public Map<String, String> getPostParameters() {
        return postParameters;
    }

    public String getPostParameter(String key) {
        if(postParameters==null){
            return null;
        }

        return postParameters.get(key);
    }

    public void setPostParameters(Map<String, String> postParameters) {
        this.postParameters = postParameters;
    }

    public List<MultipartPart> getMultipartData() {
        return multipartData;
    }

    public void setMultipartData(List<MultipartPart> multipartData) {
        this.multipartData = multipartData;
    }

    public boolean hasBody(){
        return getRequest()!=null;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public String getRequestParam(String key){
        var result = getPostParameter(key);
        if(result==null){
            result = getQuery(key);
        }
        if(result== null){
            result = getHeader(key);
        }
        return result;
    }

    public Map<String, String> getPathParameters() {
        return pathParameters;
    }

    public void setPathParameters(Map<String, String> pathParameters) {
        this.pathParameters = pathParameters;
    }

    public String getPathParameter(String id) {
        if(pathParameters==null)return null;
        if(pathParameters.containsKey(id))return pathParameters.get(id);
        for (var kvp : pathParameters.entrySet()) {
            if(kvp.getKey().equalsIgnoreCase(id)) return kvp.getValue();
        }
        return null;
    }

    public long getMs() {
        return ms;
    }
}
