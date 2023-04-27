package org.kendar.servers.dbproxy;

public class DbProxy {

    private String id;
    private String driver;
    private DbDescriptor exposed;
    private DbDescriptor remote;
    private boolean active;
    public DbProxy() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public DbDescriptor getExposed() {
        return exposed;
    }

    public void setExposed(DbDescriptor exposed) {
        this.exposed = exposed;
    }

    public DbDescriptor getRemote() {
        return remote;
    }

    public void setRemote(DbDescriptor remote) {
        this.remote = remote;
    }

    public DbProxy copy() {
        var result = new DbProxy();
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
