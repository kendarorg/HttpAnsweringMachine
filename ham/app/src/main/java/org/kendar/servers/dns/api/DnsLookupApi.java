package org.kendar.servers.dns.api;

import org.kendar.http.FilteringClass;
import org.kendar.http.annotations.HttpTypeFilter;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "*", blocking = true)
public class DnsLookupApi implements FilteringClass {
    @Override
    public String getId() {
        return this.getClass().getName();
    }
}
