package org.kendar.servers.http.api.model;

import java.util.HashMap;
import java.util.List;

public class RestFilter {
    private boolean blocking;
    private Long id;
    private String phase;
    private int priority;
    private String name;
    private String source;
    private String type;
    private HashMap<String, String> matchers;
    private List<RestFilterRequire> require;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public HashMap<String, String> getMatchers() {
        return matchers;
    }

    public void setMatchers(HashMap<String, String> matchers) {

        this.matchers = matchers;
    }

    public List<RestFilterRequire> getRequire() {
        return require;
    }

    public void setRequire(List<RestFilterRequire> require) {
        this.require = require;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }
}
