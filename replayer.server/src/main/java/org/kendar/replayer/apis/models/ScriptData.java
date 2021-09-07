package org.kendar.replayer.apis.models;

public class ScriptData {
    private String description;
    private String id;
    private RedirectFilter filter;

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
}
