package org.kendar.servers.http.api.model;

public class RestFilterRequire {
    private String name;
    private boolean binary;

    public boolean isBinary() {
        return binary;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
