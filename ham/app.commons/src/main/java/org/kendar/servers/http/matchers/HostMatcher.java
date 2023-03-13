package org.kendar.servers.http.matchers;

public interface HostMatcher {
    String getHostAddress();

    void setHostAddress(String address);
}
