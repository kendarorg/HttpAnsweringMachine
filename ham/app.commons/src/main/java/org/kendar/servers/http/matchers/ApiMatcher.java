package org.kendar.servers.http.matchers;

import org.kendar.servers.http.Request;

import java.util.function.Function;
import java.util.regex.Pattern;

public class ApiMatcher implements FilterMatcher, PathMatcher, HostMatcher {

    private final PathSimpleMatcher pathSimpleMatchers = new PathSimpleMatcher();
    private final PathRegexpMatcher pathMatchers = new PathRegexpMatcher();
    private String hostAddress;
    private String hostPattern;
    private String method;
    private Pattern hostPatternReal;
    private Pattern pathPatternReal;
    private String pathPattern;
    private String pathAddress;

    public ApiMatcher() {

    }

    public ApiMatcher(String hostAddress, String hostPattern, String method, String pathPattern, String pathAddress) {
        this.hostAddress = hostAddress;
        this.hostPattern = hostPattern;
        this.method = method;
        this.pathPattern = pathPattern;
        this.pathAddress = pathAddress;
    }

    public Pattern getHostPatternReal() {
        return hostPatternReal;
    }

    public Pattern getPathPatternReal() {
        return pathPatternReal;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public String getHostPattern() {
        return hostPattern;
    }

    public void setHostPattern(String hostPattern) {
        this.hostPattern = hostPattern;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    public String getPathAddress() {
        return pathAddress;
    }

    public void setPathAddress(String pathAddress) {
        this.pathAddress = pathAddress;
    }

    @Override
    public boolean matches(Request req) {
        if (pathSimpleMatchers != null && pathSimpleMatchers.notMatch(req.getMethod(), this.method)) {
            return false;
        }
        if (this.hostPatternReal != null && !hostPatternReal.matcher(req.getHost()).matches()) {
            return false;
        } else if (pathSimpleMatchers.notMatch(req.getHost(), this.hostAddress)) {
            return false;
        }

        if (pathMatchers != null && pathMatchers.matches(req, pathPatternReal)) {
            return true;
        }

        if (pathSimpleMatchers != null && pathSimpleMatchers.matches(req)) {
            return true;
        }
        if (pathSimpleMatchers != null && pathSimpleMatchers.notMatch(req.getPath(), this.pathAddress)) {
            return false;
        }

        return true;
    }


    @Override
    public void initialize(Function<String, String> apply) {
        if (hostAddress != null) hostAddress = apply.apply(hostAddress);
        if (hostPattern != null) hostPattern = apply.apply(hostPattern);
        if (pathAddress != null) {
            pathAddress = apply.apply(pathAddress);
            pathSimpleMatchers.setupPathSimpleMatchers(pathAddress);
        }
        if (pathPattern != null) pathPattern = apply.apply(pathPattern);
        if (hostPattern != null && !hostPattern.isEmpty()) {
            hostPatternReal = Pattern.compile(hostPattern);
        }
        if (pathPattern != null && !pathPattern.isEmpty()) {
            pathPatternReal = Pattern.compile(pathPattern);
            pathMatchers.getNamedGroupCandidates(pathPattern);
        }
    }

    @Override
    public boolean validate() {
        return (isValid(pathAddress) || isValid(pathPattern) || isValid(hostAddress) || isValid(hostPattern)) && isValid(method);
    }

    private boolean isValid(String val) {
        return val != null && val.length() > 0;
    }
}
