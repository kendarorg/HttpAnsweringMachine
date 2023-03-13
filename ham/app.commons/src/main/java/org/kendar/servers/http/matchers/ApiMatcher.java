package org.kendar.servers.http.matchers;

import org.kendar.servers.http.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ApiMatcher implements FilterMatcher,PathMatcher,HostMatcher{

    public ApiMatcher(){

    }

    private String hostAddress;
    private String hostPattern;
    private String method;
    private Pattern hostPatternReal;

    private PathSimpleMatcher pathSimpleMatchers = new PathSimpleMatcher();
    private PathRegexpMatcher pathMatchers = new PathRegexpMatcher();

    public Pattern getHostPatternReal() {
        return hostPatternReal;
    }

    public Pattern getPathPatternReal() {
        return pathPatternReal;
    }

    private Pattern pathPatternReal;

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public void setHostPattern(String hostPattern) {
        this.hostPattern = hostPattern;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    public void setPathAddress(String pathAddress) {
        this.pathAddress = pathAddress;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public String getHostPattern() {
        return hostPattern;
    }

    public String getMethod() {
        return method;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public String getPathAddress() {
        return pathAddress;
    }

    private String pathPattern;
    private String pathAddress;

    public ApiMatcher(String hostAddress, String hostPattern, String method, String pathPattern, String pathAddress) {
        this.hostAddress = hostAddress;
        this.hostPattern = hostPattern;
        this.method = method;
        this.pathPattern = pathPattern;
        this.pathAddress = pathAddress;
    }



    @Override
    public boolean matches(Request req) {
        if(pathSimpleMatchers!=null && pathSimpleMatchers.notMatch(req.getMethod(),this.method)){
            return false;
        }
        if(this.hostPatternReal!=null && !hostPatternReal.matcher(req.getHost()).matches()) {
            return false;
        }else if(pathSimpleMatchers.notMatch(req.getHost(),this.hostAddress)){
            return false;
        }

        if(pathMatchers!=null && pathMatchers.matches(req,pathPatternReal)){
            return true;
        }

        if(pathSimpleMatchers!=null && pathSimpleMatchers.matches(req)){
            return true;
        }
        if(pathSimpleMatchers!=null && pathSimpleMatchers.notMatch(req.getPath(),this.pathAddress)){
            return false;
        }

        return true;
    }



    @Override
    public void initialize(Function<String, String> apply) {
        if(hostAddress!=null)hostAddress = apply.apply(hostAddress);
        if(hostPattern!=null)hostPattern = apply.apply(hostPattern);
        if(pathAddress!=null){
            pathAddress = apply.apply(pathAddress);
            pathSimpleMatchers.setupPathSimpleMatchers(pathAddress);
        }
        if(pathPattern!=null)pathPattern = apply.apply(pathPattern);
        if(hostPattern!=null && !hostPattern.isEmpty()){
            hostPatternReal = Pattern.compile(hostPattern);
        }
        if(pathPattern!=null && !pathPattern.isEmpty()){
            pathPatternReal = Pattern.compile(pathPattern);
            pathMatchers.getNamedGroupCandidates(pathPattern);
        }
    }

    @Override
    public boolean validate() {
        return (isValid(pathAddress)||isValid(pathPattern)||isValid(hostAddress)||isValid(hostPattern))&& isValid(method);
    }

    private boolean isValid(String val) {
        return val!=null&&val.length()>0;
    }
}
