package org.kendar.replayer.apis.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScriptData {
    private String description;
    private String name;
    private String id;
    private HashMap<String, String> filter;
    private List<Integer> stimulatorTest = new ArrayList<>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<String, String> getFilter() {
        return filter;
    }

    public void setFilter(HashMap<String, String> filter) {
        this.filter = filter;
    }

    public List<Integer> getStimulatorTest() {
        return stimulatorTest;
    }

    public void setStimulatorTest(List<Integer> stimulatorTest) {
        this.stimulatorTest = stimulatorTest;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
