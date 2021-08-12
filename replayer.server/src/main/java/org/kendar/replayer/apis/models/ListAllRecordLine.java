package org.kendar.replayer.apis.models;

import org.kendar.replayer.storage.ReplayerRow;

import java.util.Map;

public class ListAllRecordLine {
    private int id;
    private String protocol;
    private String path;
    private String host;
    private int port;
    private String method;
    private Map<String,String> query;
    private Map<String,String> headers;
    private boolean hasRequestBody;
    private boolean hasResponseBody;
    private int httpCode;

    public ListAllRecordLine(ReplayerRow staticLine) {
        setId(staticLine.getId());
        setProtocol(staticLine.getRequest().getProtocol());
        setPath(staticLine.getRequest().getPath());
        setHost(staticLine.getRequest().getHost());
        setMethod(staticLine.getRequest().getMethod());
        setQuery(staticLine.getRequest().getQuery());
        setHeaders(staticLine.getRequest().getHeaders());
        setHasRequestBody(!"0".equalsIgnoreCase(staticLine.getRequestHash()));
        setHasResponseBody(!"0".equalsIgnoreCase(staticLine.getResponseHash()));
        setHttpCode(staticLine.getResponse().getStatusCode());
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getQuery() {
        return query;
    }


    public Map<String, String> getHeaders() {
        return headers;
    }

    public boolean isHasRequestBody() {
        return hasRequestBody;
    }

    public void setHasRequestBody(boolean hasRequestBody) {
        this.hasRequestBody = hasRequestBody;
    }

    public boolean isHasResponseBody() {
        return hasResponseBody;
    }

    public void setHasResponseBody(boolean hasResponseBody) {
        this.hasResponseBody = hasResponseBody;
    }

    public int getId() {
        return id;
    }

    public void setQuery(Map<String, String> query) {
        this.query = query;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public void setId(int id) {
        this.id = id;
    }
}
