package org.kendar.http;

import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.http.matchers.FilterMatcher;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class GenericFilterExecutor {
    private final int priority;
    private final boolean methodBlocking;
    private final boolean typeBlocking;

    private final HttpFilterType phase;
    private final Method callback;
    private final FilteringClass filterClass;
    private List<FilterMatcher> matchers = new ArrayList<>();
    private String id;

    public GenericFilterExecutor(int priority, boolean methodBlocking,
                                 boolean typeBlocking, HttpFilterType phase,
                                 Method callback, FilteringClass filterClass, FilterMatcher... matcher) {
        this.matchers.addAll(Arrays.asList(matcher));
        if (filterClass != null) {
            this.id = filterClass.getId();
        }
        this.priority = priority;
        this.methodBlocking = methodBlocking;
        this.typeBlocking = typeBlocking;
        this.phase = phase;
        this.callback = callback;
        this.filterClass = filterClass;
    }

    public abstract boolean run(Request request, Response response);

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isMethodBlocking() {
        return methodBlocking;
    }

    public boolean isTypeBlocking() {
        return typeBlocking;
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

    public List<FilterMatcher> getMatchers() {
        return matchers;
    }

    public void setMatcher(List<FilterMatcher> matcher) {
        this.matchers = matcher;
    }
}
