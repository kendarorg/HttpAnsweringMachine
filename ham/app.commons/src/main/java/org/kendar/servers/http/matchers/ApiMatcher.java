package org.kendar.servers.http.matchers;

import org.kendar.servers.http.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ApiMatcher implements FilterMatcher{

    public ApiMatcher(){

    }
    private static final Pattern namedGroupsPattern =
            Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z\\d]*)>");
    private String hostAddress;
    private String hostPattern;
    private String method;
    private Pattern hostPatternReal;

    private List<String> pathSimpleMatchers = new ArrayList<>();
    private List<String> pathMatchers;

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

    private List<String> getNamedGroupCandidates() {
        Set<String> matchedGroups = new TreeSet<>();
        var m = namedGroupsPattern.matcher(pathPattern);
        while (m.find()) {
            matchedGroups.add(m.group(1));
        }
        return new ArrayList<>(matchedGroups);
    }

    @Override
    public boolean matches(Request req) {
        if(notMatch(req.getMethod(),this.method)){
            return false;
        }
        if(this.hostPatternReal!=null && !hostPatternReal.matcher(req.getHost()).matches()) {
            return false;
        }else if(notMatch(req.getHost(),this.hostAddress)){
            return false;
        }

        if (pathPatternReal != null) {
            var matcher = pathPatternReal.matcher(req.getPath());
            if (matcher.matches()) {
                for (int i = 0; i < pathMatchers.size(); i++) {
                    var group = matcher.group(pathMatchers.get(i));
                    if (group != null) {
                        req.addPathParameter(pathMatchers.get(i), group);
                    }
                }
                return true;
            }
        }
        if (pathSimpleMatchers!=null && pathSimpleMatchers.size() > 0) {
            var explPath = req.getPath().split("/");
            if (pathSimpleMatchers.size() != explPath.length) return false;
            for (var i = 0; i < pathSimpleMatchers.size(); i++) {
                var partTemplate = pathSimpleMatchers.get(i);
                var partPath = explPath[i];
                if (partTemplate.startsWith("*")) {
                    partTemplate = partTemplate.substring(1);
                    req.addPathParameter(partTemplate, partPath);
                } else if (!partTemplate.equalsIgnoreCase(partPath)) {
                    return false;
                }
            }
            return true;
        }
        if(notMatch(req.getPath(),this.pathAddress)){
            return false;
        }

        return true;
    }

    private boolean notMatch(String real, String provided) {
        if(provided==null)return false;
        if(provided.equalsIgnoreCase("*"))return false;
        if(real.equalsIgnoreCase(provided))return false;
        return true;
    }

    @Override
    public void initialize(Function<String, String> apply) {
        if(hostAddress!=null)hostAddress = apply.apply(hostAddress);
        if(hostPattern!=null)hostPattern = apply.apply(hostPattern);
        if(pathAddress!=null){
            pathAddress = apply.apply(pathAddress);
            pathSimpleMatchers = setupPathSimpleMatchers();
        }
        if(pathPattern!=null)pathPattern = apply.apply(pathPattern);
        if(hostPattern!=null && !hostPattern.isEmpty()){
            hostPatternReal = Pattern.compile(hostPattern);
        }
        if(pathPattern!=null && !pathPattern.isEmpty()){
            pathPatternReal = Pattern.compile(pathPattern);
            pathMatchers = getNamedGroupCandidates();
        }
    }

    private List<String> setupPathSimpleMatchers() {
        var result = new ArrayList<String>();
        if (pathAddress!=null && pathAddress.contains("{")) {
            var explTemplate = pathAddress.split("/");
            for (var i = 0; i < explTemplate.length; i++) {
                var partTemplate = explTemplate[i];
                if (partTemplate.startsWith("{")) {
                    partTemplate = partTemplate.substring(1);
                    partTemplate = "*" + partTemplate.substring(0, partTemplate.length() - 1);
                }
                result.add(partTemplate);
            }
        }
        return result;
    }
}
