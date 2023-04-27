package org.kendar.ham;

import java.util.regex.Pattern;

public class ApiMatcherBuilder {


    private Methods method;
    private String hostAddress;
    private String hostPattern;
    private String pathAddress;
    private String pathPattern;

    public ApiMatcherBuilder withMethod(Methods method) {
        this.method = method;
        return this;
    }

    public ApiMatcherBuilder withHost(String host) {
        this.hostAddress = host;
        return this;
    }

    public ApiMatcherBuilder wihtHostPattern(String host) {
        this.hostPattern = host;
        return this;
    }

    public ApiMatcherBuilder verifyHostPattern(String host) {
        var pattern = Pattern.compile(hostPattern);
        var matcher = pattern.matcher(host);
        if (!matcher.matches()) {
            throw new RuntimeException(host + " does not match " + hostPattern);
        }
        return this;
    }

    public ApiMatcherBuilder withPath(String host) {
        this.pathAddress = host;
        return this;
    }

    public ApiMatcherBuilder withPathPattern(String host) {
        this.pathPattern = host;
        return this;
    }

    public ApiMatcherBuilder verifyPathPattern(String host) {
        var pattern = Pattern.compile(pathPattern);
        var matcher = pattern.matcher(host);
        if (!matcher.matches()) {
            throw new RuntimeException(host + " does not match " + hostPattern);
        }
        return this;
    }

    public Matcher build() {
        var matcher = new JsBuilder.ApiMatcher();
        matcher.setMethod(this.method);
        matcher.setHostAddress(this.hostAddress);
        matcher.setHostPattern(this.hostPattern);
        matcher.setPathAddress(this.pathAddress);
        matcher.setPathPattern(this.pathPattern);
        return matcher;
    }
}
