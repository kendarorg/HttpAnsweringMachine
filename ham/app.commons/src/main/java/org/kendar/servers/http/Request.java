package org.kendar.servers.http;

import java.util.*;
import java.util.stream.Collectors;

public class Request {

    private long ms = Calendar.getInstance().getTimeInMillis();
    private boolean binaryRequest;
    private String method;
    private String requestText;
    private byte[] requestBytes;
    private Map<String,String> headers;
    private String protocol;
    private boolean soapRequest;
    private String basicPassword;
    private String basicUsername;
    private List<MultipartPart> multipartData = new ArrayList<>();
    private boolean staticRequest;
    private String host;
    private String path;
    private Map<String,String> postParameters = new HashMap<>();
    private int port;
    private Map<String,String> query = new HashMap<>();
    private String remoteHost;
    private Map<String, String> pathParameters = new HashMap<>();
    private Request original;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRequestText() {
        return requestText;
    }

    public void setRequestText(String requestText) {
        this.requestText = requestText;
    }

    public byte[] getRequestBytes() {
        return requestBytes;
    }

    public void setRequestBytes(byte[] requestBytes) {
        this.requestBytes = requestBytes;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public boolean isSoapRequest() {
        return soapRequest;
    }

    public void setSoapRequest(boolean soapRequest) {
        this.soapRequest = soapRequest;
    }

    public String getBasicPassword() {
        return basicPassword;
    }

    public void setBasicPassword(String basicPassword) {
        this.basicPassword = basicPassword;
    }

    public String getBasicUsername() {
        return basicUsername;
    }

    public void setBasicUsername(String basicUsername) {
        this.basicUsername = basicUsername;
    }

    public List<MultipartPart> getMultipartData() {
        return multipartData;
    }

    public void setMultipartData(List<MultipartPart> multipartData) {
        this.multipartData = multipartData;
    }

    public boolean isStaticRequest() {
        return staticRequest;
    }

    public void setStaticRequest(boolean staticRequest) {
        this.staticRequest = staticRequest;
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

    public Map<String, String> getPostParameters() {
        return postParameters;
    }

    public void setPostParameters(Map<String, String> postParameters) {
        this.postParameters = postParameters;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Map<String, String> getQuery() {
        return query;
    }

    public void setQuery(Map<String, String> query) {
        this.query = query;
    }

    public boolean isBinaryRequest() {
        return binaryRequest;
    }

    public void setBinaryRequest(boolean binaryRequest) {
        this.binaryRequest = binaryRequest;
    }

    public String getHeader(String id) {

        return RequestUtils.getFromMap(this.headers,id);
    }

    public void addPathParameter(String key, String value) {
        if(this.pathParameters==null) this.pathParameters = new HashMap<>();
        RequestUtils.addToMap(this.pathParameters,key,value);
    }

    public String getPathParameter(String id) {
        return RequestUtils.getFromMap(this.pathParameters,id);
    }

    public String getRequestParameter(String key) {
        var result = getPostParameter(key);
        if(result==null){
            result = getQuery(key);
        }
        if(result== null){
            result = getHeader(key);
        }
        if(result== null){
            result = getPathParameter(key);
        }
        return result;
    }
    public void addHeader(String key, String value) {
        if(this.headers==null) this.headers = new HashMap<>();
        RequestUtils.addToMap(this.headers,key,value);
    }

    public void addQuery(String key, String value) {

        if(this.query==null) this.query = new HashMap<>();
        RequestUtils.addToMap(this.query,key,value);
    }

    public String getQuery(String id) {
        return RequestUtils.getFromMap(this.query,id);
    }

    public String getPostParameter(String id) {
        return RequestUtils.getFromMap(this.postParameters,id);
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public long getMs() {
        return ms;
    }

    public void setMs(long ms) {
        this.ms = ms;
    }

    public Map<String, String> getPathParameters() {
        return pathParameters;
    }

    public void setPathParameters(Map<String, String> pathParameters) {
        this.pathParameters = pathParameters;
    }

    public Request copy() {
        var r  = new Request();
        r.pathParameters = this.pathParameters.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        r.ms = this.ms;
        r.remoteHost = this.remoteHost;
        r.path = this.path;
        r.basicPassword = this.basicPassword;
        r.basicUsername = this.basicUsername;
        r.binaryRequest = this.binaryRequest;
        if(headers!=null) {
            r.headers = this.headers.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        r.host = this.host;
        r.method = this.method;
        if(multipartData!=null) {
            r.multipartData = this.multipartData.stream().map(multipartPart -> multipartPart.copy()).collect(Collectors.toList());
        }
        r.port = this.port;
        if(postParameters!=null) {
            r.postParameters = this.postParameters.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        r.protocol = this.protocol;
        if(query!=null) {
            r.query = this.query.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        r.requestBytes = this.requestBytes!=null?this.requestBytes.clone():this.requestBytes;
        r.requestText = this.requestText!=null?new String(this.requestText):this.requestText;
        r.soapRequest= this.soapRequest;
        r.staticRequest= this.staticRequest;
        return r;
    }

    public void addOriginal(Request oriSource) {
        this.original = oriSource;
    }

    public Request retrieveOriginal(){
        if(original!=null) return original;
        return this;
    }

    public String findCookie(String value){
        var cookies = this.getHeader("Cookie");
        if(cookies==null) return null;
        var splittedCookies = cookies.split(";");
        for(var cookie:splittedCookies){
            var cookieData = cookie.trim().split("=");
            if(value.equalsIgnoreCase(cookieData[0].trim())){
                return cookieData[1].trim();
            }
        }
        return null;
    }
}
