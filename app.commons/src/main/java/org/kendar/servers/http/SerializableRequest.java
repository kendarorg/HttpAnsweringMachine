package org.kendar.servers.http;

import java.util.List;
import java.util.Map;

public class SerializableRequest {

    boolean binaryRequest;
    String method;
    String requestText;
    byte[] requestBytes;
    Map<String,String> headers;
    String protocol;
    boolean soapRequest;
    String basicPassword;
    String basicUsername;
    List<MultipartPart> multipartData;
    boolean staticRequest;
    String host;
    String path;
    Map<String,String> postParameters;
    int port;
    Map<String,String> query;

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
}
