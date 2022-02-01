package org.kendar.docker.builder;

import java.util.ArrayList;
import java.util.List;

public class DockerAddOn {
    private String description;
    private String id;
    private List<String> content = new ArrayList<>();

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

    public List<String> getContent() {
        return content;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }
}
