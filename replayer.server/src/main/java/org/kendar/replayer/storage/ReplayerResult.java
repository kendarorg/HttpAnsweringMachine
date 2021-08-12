package org.kendar.replayer.storage;

import java.util.ArrayList;
import java.util.List;

public class ReplayerResult {
    private List<ReplayerRow> staticRequests = new ArrayList<>();
    private List<ReplayerRow> dynamicRequests = new ArrayList<>();
    private List<String> errors = new ArrayList<>();
    private String description;
    public void add(ReplayerRow row) {
        getStaticRequests().add(row);
    }

    public void addError(String error) {
        getErrors().add(error);
    }

    public List<ReplayerRow> getStaticRequests() {
        return staticRequests;
    }

    public void setStaticRequests(List<ReplayerRow> staticRequests) {
        this.staticRequests = staticRequests;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ReplayerRow> getDynamicRequests() {
        return dynamicRequests;
    }

    public void setDynamicRequests(List<ReplayerRow> dynamicRequests) {
        this.dynamicRequests = dynamicRequests;
    }
}
