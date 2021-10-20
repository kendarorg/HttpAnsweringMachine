package org.kendar.servers.config;

import java.util.List;

public class SSLConfig {
    private String cname;
    private List<SSLDomain> domains;

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public List<SSLDomain> getDomains() {
        return domains;
    }

    public void setDomains(List<SSLDomain> domains) {
        this.domains = domains;
    }
}
