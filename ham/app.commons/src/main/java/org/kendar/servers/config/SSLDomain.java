package org.kendar.servers.config;

import org.kendar.servers.Copyable;

import java.util.Objects;

public class SSLDomain implements Copyable<SSLDomain> {
    private String id;
    private String address;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SSLDomain sslDomain = (SSLDomain) o;
        return address.equals(sslDomain.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }

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
