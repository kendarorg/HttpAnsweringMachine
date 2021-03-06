package org.kendar.replayer.apis.models;

import java.util.ArrayList;
import java.util.List;

public class ScriptData {
    private String description;
    private String id;
    private RedirectFilter filter;
    private List<Integer> stimulatorTest = new ArrayList<>();
    private List<Integer> pactTest = new ArrayList<>();
    private List<Integer> stimulatedTest = new ArrayList<>();

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

    public RedirectFilter getFilter() {
        return filter;
    }

    public void setFilter(RedirectFilter filter) {
        this.filter = filter;
    }

    public List<Integer> getStimulatorTest() {
        return stimulatorTest;
    }

    public void setStimulatorTest(List<Integer> stimulatorTest) {
        this.stimulatorTest = stimulatorTest;
    }

    public List<Integer> getPactTest() {
        return pactTest;
    }

    public void setPactTest(List<Integer> pactTest) {
        this.pactTest = pactTest;
    }

    public List<Integer> getStimulatedTest() {
        return stimulatedTest;
    }

    public void setStimulatedTest(List<Integer> stimulatedTest) {
        this.stimulatedTest = stimulatedTest;
    }
}
