package org.kendar.http.annotations.concrete;

import org.kendar.http.annotations.HamMatcher;
import org.kendar.http.annotations.MatcherFunction;
import org.kendar.http.annotations.MatcherType;

import java.lang.annotation.Annotation;

public class HamMatcherConcrete implements HamMatcher {
    private HamMatcher matcher;

    public HamMatcherConcrete(HamMatcher matcher) {

        this.matcher = matcher;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }

    @Override
    public String value() {
        return matcher.value();
    }

    @Override
    public MatcherFunction function() {
        return matcher.function();
    }

    @Override
    public MatcherType type() {
        return matcher.type();
    }

    @Override
    public String id() {
        if (matcher.id().length() > 0) {
            return matcher.id();
        }
        return null;
    }
}
