package org.kendar.servers.http.matchers;

import org.kendar.servers.http.Request;

import java.util.function.Function;

public class SimplePathMatcher implements FilterMatcher {
    public String getPathPattern() {
        return pathPattern;
    }

    public String getPathAddress() {
        return pathAddress;
    }

    private final String pathPattern;
    private final String pathAddress;

    public SimplePathMatcher(String pathPattern, String pathAddress) {
        this.pathPattern = pathPattern;
        this.pathAddress = pathAddress;
    }

    @Override
    public boolean matches(Request req) {
        return false;
    }

    @Override
    public void initialize(Function<String, String> apply) {

    }
}
