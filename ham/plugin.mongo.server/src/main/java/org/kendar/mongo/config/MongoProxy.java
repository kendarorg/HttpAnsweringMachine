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

    private int exposedPort;

    public int getExposedPort() {
        return exposedPort;
    }

    public void setExposedPort(int exposedPort) {
        this.exposedPort = exposedPort;
    }

    private MongoDescriptor remote;
    private boolean active;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
        result.setExposedPort(this.exposedPort);
        result.setRemote(this.remote.copy());
        return result;
    }
}
