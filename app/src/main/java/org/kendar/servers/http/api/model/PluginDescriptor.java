package org.kendar.servers.http.api.model;

public class PluginDescriptor {
    private String address;
    private String description;

    public PluginDescriptor(String address, String description){

        this.address = address;
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
