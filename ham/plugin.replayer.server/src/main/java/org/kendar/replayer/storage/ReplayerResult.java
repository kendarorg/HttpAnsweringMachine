package org.kendar.replayer.storage;

import org.kendar.replayer.apis.models.RedirectFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReplayerResult {
    private List<ReplayerRow> staticRequests = new ArrayList<>();
    private List<ReplayerRow> dynamicRequests = new ArrayList<>();
    private List<String> errors = new ArrayList<>();
    private List<CallIndex> indexes = new ArrayList<>();
    private HashMap<String,String> variables = new HashMap<>();
    private HashMap<String,String> preScript = new HashMap<>();
    private HashMap<String,String> postScript = new HashMap<>();
    private String initScript;

    private String description;
    private RedirectFilter filter;

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

    public void setFilter(RedirectFilter filter) {
        this.filter = filter;
    }

    public RedirectFilter getFilter() {
        return filter;
    }

    public List<CallIndex> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<CallIndex> indexes) {
        this.indexes = indexes;
    }

    public HashMap<String, String> getVariables() {
        return variables;
    }

    public void setVariables(HashMap<String, String> variables) {
        this.variables = variables;
    }

    public String getInitScript() {
        return initScript;
    }

    public void setInitScript(String initScript) {
        this.initScript = initScript;
    }

    public HashMap<String, String> getPreScript() {
        return preScript;
    }

    public void setPreScript(HashMap<String, String> preScript) {
        this.preScript = preScript;
    }

    public HashMap<String, String> getPostScript() {
        return postScript;
    }

    public void setPostScript(HashMap<String, String> postScript) {
        this.postScript = postScript;
    }
}
