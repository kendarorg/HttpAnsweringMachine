package org.kendar.servers.proxy;

public class RemoteServerStatus {
    private int id;
    private String when;
    private String where;
    private String test;
    private boolean running = false;

    public RemoteServerStatus(int id,String when, String where, String test) {
        this.id = id;
        this.when = when;
        this.where = where;
        this.test = test;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
