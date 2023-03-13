package org.kendar.servers.http.api.model;

import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.IdBuilder;

public class FilterDto {
    private final String hostAddress;
    private final String hostPattern;
    private final int priority;
    private final String method;
    private final String pathAddress;
    private final String pathPattern;
    private final HttpFilterType phase;
    private final boolean blocking;
    private boolean enabled;
    private final String id;

    public FilterDto(boolean enabled, HttpTypeFilter type, HttpMethodFilter method, String filterClass) {
        this.enabled = enabled;
        this.id = IdBuilder.buildId(type, method, filterClass);
        blocking = type.blocking() || method.blocking();
        hostAddress = type.hostAddress();
        hostPattern = type.hostPattern();
        priority = type.priority();
        this.method = method.method();
        pathAddress = method.pathAddress();
        pathPattern = method.pathPattern();
        phase = method.phase();
    }


    public String getHostAddress() {
        return hostAddress;
    }

    public String getHostPattern() {
        return hostPattern;
    }

    public int getPriority() {
        return priority;
    }

    public String getMethod() {
        return method;
    }

    public String getPathAddress() {
        return pathAddress;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public HttpFilterType getPhase() {
        return phase;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public String getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
