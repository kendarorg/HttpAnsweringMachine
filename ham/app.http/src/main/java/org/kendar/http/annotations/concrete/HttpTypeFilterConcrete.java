package org.kendar.http.annotations.concrete;

import org.kendar.http.annotations.HttpTypeFilter;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

public class HttpTypeFilterConcrete implements HttpTypeFilter {

    private String hostAddress;
    private boolean blocking;
    private int priority;
    private Pattern hostPattern;

    public HttpTypeFilterConcrete(String hostAddress, boolean blocking, int priority, Pattern hostPattern){

        this.hostAddress = hostAddress;
        this.blocking = blocking;
        this.priority = priority;
        this.hostPattern = hostPattern;
    }
    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }

    @Override
    public String hostAddress() {
        return hostAddress;
    }

    @Override
    public String hostPattern() {
        if (hostPattern == null) return null;
        return hostPattern.toString();
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public boolean blocking() {
        return blocking;
    }
}
