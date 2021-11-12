package org.kendar.servers.config;

import org.kendar.servers.Copyable;

public class SSLDomain implements Copyable<SSLDomain> {
    private String id;
    private String address;

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

    @Override public SSLDomain copy() {
        var result = new SSLDomain();
        result.id = this.id;
        result.address = this.address;
        return result;
    }
}
