package org.kendar.replayer.apis.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SingleScript {
    private List<SingleScriptLine> lines = new ArrayList<>();
    private HashMap<String, String> filter;
    private Long id;
    private String description;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SingleScriptLine> getLines() {
        return lines;
    }

    public void setLines(List<SingleScriptLine> lines) {
        this.lines = lines;
    }

    public HashMap<String, String> getFilter() {
        return filter;
    }

    public void setFilter(HashMap<String, String> filter) {
        this.filter = filter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
