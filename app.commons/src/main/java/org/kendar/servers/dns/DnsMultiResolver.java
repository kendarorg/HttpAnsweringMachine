package org.kendar.servers.dns;

import java.util.List;

public interface DnsMultiResolver {
    List<String> resolve(String dnsName,boolean fromLocalhost);
    void verify();
    List<String> resolveLocal(String dnsName);
    List<String> resolveRemote(String dnsName,boolean fromLocalhost);
    void setExtraServers(List<DnsServerDescriptor>  extraServers);
    List<DnsServerDescriptor> getExtraServers();
}
