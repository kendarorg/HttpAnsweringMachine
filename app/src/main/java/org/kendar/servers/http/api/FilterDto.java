package org.kendar.servers.http.api;

import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;

public class FilterDto {
    private final String hostAddress;
    private final String hostPattern;
    private final int priority;
    private final String method;
    private final String pathAddress;
    private final String pathPattern;
    private final HttpFilterType phase;
    private final boolean blocking;
    private final String id;

    public FilterDto(String id,HttpTypeFilter type, HttpMethodFilter method) {
        this.id = id;
        blocking = type.blocking()||method.blocking();
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
}
