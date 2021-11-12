package org.kendar.dns.configurations;

public class ExtraDnsServer {
    private String id;
    private String address;
    private String resolved;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ExtraDnsServer copy() {
        var result = new ExtraDnsServer();
        result.id = this.id;
        result.address = this.address;
        result.resolved = this.resolved;
        return result;
    }

    public String getResolved() {
        return resolved;
    }

    public void setResolved(String resolved) {
        this.resolved = resolved;
    }
}
