package org.kendar.http.annotations.concrete;

import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamMatcher;
import org.kendar.http.annotations.HttpMethodFilter;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.regex.Pattern;

@SuppressWarnings("ClassExplicitlyAnnotation")
public class HttpMethodFilterConcrete implements HttpMethodFilter {
    private final HttpFilterType phase;
    private final boolean methodBlocking;
    private final String pathAddress;
    private final Pattern pathPattern;
    private final String method;
    private final String description;
    private final String id;
    private final HamMatcher[] extraMatches;

    public HttpMethodFilterConcrete(HttpFilterType phase, boolean methodBlocking,
                                    String pathAddress, Pattern pathPattern,
                                    String method, String description,
                                    String id, HamMatcher[] extraMatches) {

        this.phase = phase;
        this.methodBlocking = methodBlocking;
        this.pathAddress = pathAddress;
        this.pathPattern = pathPattern;
        this.method = method;
        this.description = description;
        this.id = id;
        this.extraMatches = extraMatches;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }

    @Override
    public HttpFilterType phase() {
        return phase;
    }

    @Override
    public boolean blocking() {
        return methodBlocking;
    }

    @Override
    public String pathAddress() {
        return pathAddress;
    }

    @Override
    public String pathPattern() {
        if (pathPattern == null) return null;
        return pathPattern.toString();
    }

    @Override
    public String method() {
        return method;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public HamMatcher[] matcher() {
        var result = new ArrayList<HamMatcher>();
        if (extraMatches != null && extraMatches.length > 0) {
            for (var matcher :
                    extraMatches) {
                result.add(new HamMatcherConcrete(matcher));
            }
        }
        return result.toArray(new HamMatcher[0]);
    }
}
