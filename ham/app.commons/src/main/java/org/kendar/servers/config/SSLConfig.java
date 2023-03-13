package org.kendar.servers.config;

import org.kendar.servers.BaseJsonConfig;

import java.util.ArrayList;
import java.util.List;

@ConfigAttribute(id = "ssl")
public class SSLConfig extends BaseJsonConfig<SSLConfig> {
    private String cname;
    private List<SSLDomain> domains;

    @Override
    public boolean isSystem() {
        return true;
    }

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

    @Override
    public SSLConfig copy() {
        var result = new SSLConfig();
        result.setId(this.getId());
        result.cname = this.cname;
        result.domains = new ArrayList<>();
        List<SSLDomain> sslDomains = this.domains;
        for (int i = 0; i < sslDomains.size(); i++) {
            var domain = sslDomains.get(i);
            result.domains.add(domain.copy());

        }
        return result;
    }
}
