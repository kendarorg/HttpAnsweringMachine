package org.kendar.servers.config;

import java.util.List;

public class WebConfig {

    private WebServerConfig https;
    private WebServerConfig http;
    private SSLConfig ssl;
    private List<FilterStatus> filters;
    private List<ProxyStatus> proxies;
    private List<StaticPage> staticPages;

    public WebServerConfig getHttps() {
        return https;
    }

    public void setHttps(WebServerConfig https) {
        this.https = https;
    }

    public WebServerConfig getHttp() {
        return http;
    }

    public void setHttp(WebServerConfig http) {
        this.http = http;
    }

    public SSLConfig getSsl() {
        return ssl;
    }

    public void setSsl(SSLConfig ssl) {
        this.ssl = ssl;
    }

    public List<FilterStatus> getFilters() {
        return filters;
    }

    public void setFilters(List<FilterStatus> filters) {
        this.filters = filters;
    }

    public List<ProxyStatus> getProxies() {
        return proxies;
    }

    public void setProxies(List<ProxyStatus> proxies) {
        this.proxies = proxies;
    }

    public List<StaticPage> getStaticPages() {
        return staticPages;
    }

    public void setStaticPages(List<StaticPage> staticPages) {
        this.staticPages = staticPages;
    }
}
