package org.kendar.mongo.config;

public class MongoProxy {

    public MongoProxy() {

    }

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String driver;
    private MongoDescriptor exposed;
    private MongoDescriptor remote;
    private boolean active;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public MongoDescriptor getExposed() {
        return exposed;
    }

    public void setExposed(MongoDescriptor exposed) {
        this.exposed = exposed;
    }

    public MongoDescriptor getRemote() {
        return remote;
    }

    public void setRemote(MongoDescriptor remote) {
        this.remote = remote;
    }

    public MongoProxy copy() {
        var result = new MongoProxy();
        result.setActive(this.active);
        result.setId(this.id);
        result.setDriver(this.driver);
        result.setExposed(this.exposed.copy());
        result.setRemote(this.remote.copy());
        return result;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }
}
