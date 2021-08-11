package org.kendar.http;

import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;

import java.lang.reflect.Method;

public abstract class GenericFilterExecutor {
    private int priority;
    private String method;
    private boolean methodBlocking;
    private boolean typeBlocking;
    private String hostAddress;
    private String pathAddress;
    private String hostPattern;
    private String pathPattern;
    private HttpFilterType phase;
    private Method callback;
    private FilteringClass filterClass;
    public abstract boolean run(Request request, Response response);

    public GenericFilterExecutor(int priority, String method, boolean methodBlocking,
                                 boolean typeBlocking,
                                 String hostAddress,String pathAddress,
                                 String hostPattern,String pathPattern, HttpFilterType phase,
                                 Method callback, FilteringClass filterClass) {
        this.priority = priority;
        this.method = method;
        this.methodBlocking = methodBlocking;
        this.typeBlocking = typeBlocking;
        this.hostAddress = hostAddress;
        this.pathAddress = pathAddress;
        this.hostPattern = hostPattern;
        this.pathPattern = pathPattern;
        this.phase = phase;
        this.callback = callback;
        this.filterClass = filterClass;
    }

    public int getPriority() {
        return priority;
    }

    public String getMethod() {
        return method;
    }

    public boolean isMethodBlocking() {
        return methodBlocking;
    }

    public boolean isTypeBlocking() {
        return typeBlocking;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public String getPathAddress() {
        return pathAddress;
    }

    public String getHostPattern() {
        return hostPattern;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public HttpFilterType getPhase() {
        return phase;
    }

    public Method getCallback() {
        return callback;
    }

    public FilteringClass getFilterClass() {
        return filterClass;
    }
}
