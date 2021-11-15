package org.kendar.servers.dns;

import java.util.List;

public interface DnsMultiResolver {
    List<String> resolve(String dnsName,boolean fromLocalhost);
    List<String> resolveLocal(String dnsName);
    List<String> resolveRemote(String dnsName,boolean fromLocalhost);
}
