package org.kendar.http;

import org.apache.http.conn.HttpClientConnectionManager;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.core.env.Environment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class FilterDescriptor {

    private boolean enabled=true;
    private final int priority;
    private final String method;
    private final boolean methodBlocking;
    private final boolean typeBlocking;
    private String hostAddress;
    private String pathAddress;
    private Pattern hostPattern;
    private Pattern pathPattern;
    private HttpFilterType phase;
    private Method callback;
    private Object filterClass;
    private List<String> pathMatchers = new ArrayList<>();
    private List<String> pathSimpleMatchers = new ArrayList<>();
    private String id;

    public String getId(){
        return id;
    }

    public FilterDescriptor(HttpTypeFilter typeFilter, HttpMethodFilter methodFilter, Method callback, FilteringClass filterClass, Environment environment) {

        this.callback = callback;
        this.filterClass = filterClass;
        this.id = filterClass.getId();
        if(typeFilter.hostPattern().length()>0){
            var realHostPattern = getWithEnv(typeFilter.hostPattern(),environment);
            hostPattern = Pattern.compile(realHostPattern);
        }else{
            hostAddress= getWithEnv(typeFilter.hostAddress(),environment);
        }
        priority = typeFilter.priority();
        method = methodFilter.method();
        phase = methodFilter.phase();
        methodBlocking = methodFilter.blocking();
        typeBlocking = typeFilter.blocking();
        if(methodFilter.pathPattern().length()>0){
            var realPathPattern = getWithEnv(methodFilter.pathPattern(),environment);
            pathPattern = Pattern.compile(realPathPattern);
            pathMatchers = getNamedGroupCandidates(realPathPattern);
        }else{
            pathAddress = getWithEnv(methodFilter.pathAddress(),environment);
            setupPathSimpleMatchers();
        }
    }

    public FilterDescriptor(GenericFilterExecutor executor,Environment environment) {
        for(var method:executor.getClass().getMethods()){
            if(method.getName()=="run"){
                this.callback = method;
                break;
            }
        }
        this.filterClass = executor;
        this.id = executor.getId();
        if(null!= executor.getHostPattern() && executor.getHostPattern().length()>0){
            var realHostPattern = getWithEnv(executor.getHostPattern(),environment);
            hostPattern = Pattern.compile(realHostPattern);
        }else{
            hostAddress= getWithEnv(executor.getHostAddress(),environment);
        }
        priority = executor.getPriority();
        method = executor.getMethod();
        phase = executor.getPhase();
        methodBlocking = executor.isMethodBlocking();
        typeBlocking = executor.isTypeBlocking();
        if(null!= executor.getPathPattern() && executor.getPathPattern().length()>0){
            var realPathPattern = getWithEnv(executor.getPathPattern(),environment);
            pathPattern = Pattern.compile(realPathPattern);
            pathMatchers = getNamedGroupCandidates(realPathPattern);
        }else{
            pathAddress = getWithEnv(executor.getPathAddress(),environment);
            setupPathSimpleMatchers();
        }
    }

    private void setupPathSimpleMatchers() {
        if (pathAddress.contains("{")) {
            var explTemplate = pathAddress.split("/");
            for (var i = 0; i < explTemplate.length; i++) {
                var partTemplate = explTemplate[i];
                if (partTemplate.startsWith("{")) {
                    partTemplate = partTemplate.substring(1);
                    partTemplate = "*"+ partTemplate.substring(0, partTemplate.length() - 1);
                }
                pathSimpleMatchers.add( partTemplate);
            }
        }
    }

    public int getPriority() {
        return priority;
    }

    public String getMethod() {
        return method;
    }

    public String getWithEnv(String data,Environment env){
        if(data.startsWith("${") && data.endsWith("}")){
            var hostVar = data.substring(0,data.length()-1).substring(2);
            var defaultVar = hostVar.split(":",2);
            if(defaultVar.length==2) {
                var real = env.getProperty(defaultVar[0]);
                if(real==null){
                    real = defaultVar[1];
                }
                return real;
            }else{
                return env.getProperty(hostVar);
            }
        }else {
            return data;
        }
    }

    public boolean matchesHost(String host, Environment env) {
        if(!enabled)return false;
        if(hostAddress!=null && hostAddress.equalsIgnoreCase("*"))return true;
        if(hostPattern!=null){
            return hostPattern.matcher(host).matches();
        }
        return host.equalsIgnoreCase(hostAddress);
    }

    private static Pattern namedGroupsPattern = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");

    private static List<String> getNamedGroupCandidates(String regex) {
        Set<String> matchedGroups = new TreeSet<>();
        var m = namedGroupsPattern.matcher(regex);
        while (m.find()) {
            matchedGroups.add(m.group(1));
        }
        return new ArrayList<>(matchedGroups);
    }

    public boolean matchesPath(String path, Environment env,Request request) {
        if(!enabled)return false;
        if(pathAddress!=null && pathAddress.equalsIgnoreCase("*"))return true;
        if(pathPattern!=null){
            var matcher = pathPattern.matcher(path);
            if(matcher.matches()){
                for(int i=0;i<pathMatchers.size();i++) {
                    var group = matcher.group(pathMatchers.get(i));
                    if(group!=null) {
                        request.getPathParameters().put(pathMatchers.get(i),group);
                    }
                }
                return true;
            }
        }

        if(pathSimpleMatchers.size()>0){
            var explPath = path.split("/");
            if(pathSimpleMatchers.size()!=explPath.length) return false;
            for(var i=0;i<pathSimpleMatchers.size();i++){
                var partTemplate = pathSimpleMatchers.get(i);
                var partPath = explPath[i];
                if(partTemplate.startsWith("*")){
                    partTemplate = partTemplate.substring(1);
                    request.getPathParameters().put(partTemplate,partPath);
                }else if(!partTemplate.equalsIgnoreCase(partPath)){
                    return false;
                }
            }
            return true;
        }
        return path.equalsIgnoreCase(pathAddress);
    }

    public boolean execute(Request request, Response response, HttpClientConnectionManager connectionManager) throws InvocationTargetException, IllegalAccessException {
        if(!enabled) return false;
        if(callback.getParameterCount()==3) {
            return (boolean) callback.invoke(filterClass, request, response, connectionManager);
        }else if(callback.getParameterCount()==2) {
            return (boolean) callback.invoke(filterClass, request, response);
        }else if(callback.getParameterCount()==1) {
            return (boolean) callback.invoke(filterClass, request);
        }else if(callback.getParameterCount()==0) {
            return (boolean) callback.invoke(filterClass);
        }
        return false;
    }

    public boolean isBlocking() {
        if(!enabled)return false;
        if(this.phase == HttpFilterType.API){
            return true;
        }
        return this.methodBlocking || this.typeBlocking;
    }

    public HttpFilterType getPhase() {
        return phase;
    }

    public void setPhase(HttpFilterType phase) {
        this.phase = phase;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
