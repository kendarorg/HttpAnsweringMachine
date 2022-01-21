package org.kendar.servers.http;

import org.mozilla.javascript.Script;

import java.util.ArrayList;
import java.util.List;

public class JsFilterDescriptor {
    private String method;
    private String hostAddress;
    private String hostRegexp;
    private String pathAddress;
    private String pathRegexp;
    private String phase;
    private String root;
    private List<String> requires;
    private int priority;
    private Script compiledScript;
    private boolean blocking;
    private List<String> source = new ArrayList<>();
    private String id;

    @Override
    public String toString(){
        var host = hostAddress==null?hostRegexp:hostAddress;
        var path = pathAddress==null?pathRegexp:pathAddress;
        return phase+" "+method+" "+host+" "+path+" "+root;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public String getHostRegexp() {
        return hostRegexp;
    }

    public void setHostRegexp(String hostRegexp) {
        this.hostRegexp = hostRegexp;
    }

    public String getPathAddress() {
        return pathAddress;
    }

    public void setPathAddress(String pathAddress) {
        this.pathAddress = pathAddress;
    }

    public String getPathRegexp() {
        return pathRegexp;
    }

    public void setPathRegexp(String pathRegexp) {
        this.pathRegexp = pathRegexp;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public List<String> getRequires() {
        return requires;
    }

    public void setRequires(List<String> requires) {
        this.requires = requires;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public void setScript(Script compiledScript) {
        this.compiledScript = compiledScript;
    }

    public Script getScript(){
        return this.compiledScript;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    public void setSource(List<String> scriptSrc) {
        this.source = scriptSrc;
    }

    public List<String> getSource() {
        return source;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
