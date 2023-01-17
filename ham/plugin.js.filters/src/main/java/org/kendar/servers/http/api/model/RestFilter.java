package org.kendar.servers.http.api.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RestFilter {
    private Long id;
    private String phase;
    private int priority;
    private String name;
    private String source;
    private String type;
    private HashMap<String, String> matchers;
    private List<RestFilterRequire> require;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getPhase() {
        return phase;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }


    public HashMap<String, String> getMatchers() {
        return matchers;
    }

    public void setMatchers(HashMap<String, String> matchers) {

        this.matchers = matchers;
    }

    public void setRequire(List<RestFilterRequire> require) {
        this.require = require;
    }

    public List<RestFilterRequire> getRequire() {
        return require;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
