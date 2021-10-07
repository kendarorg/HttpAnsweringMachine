package org.kendar.dns.configurations;

import org.kendar.dns.PatternItem;
import org.kendar.servers.dns.DnsServerDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DnsConfiguration {
    public List<DnsServerDescriptor> servers = new ArrayList<>();
    public List<String> blocker = new ArrayList<>();
    public List<PatternItem> dnsRecords = new ArrayList<>();
    public HashMap<String, HashSet<String>> domains = new HashMap<>();

    public DnsConfiguration copy() {
        var result = new DnsConfiguration();
        result.blocker = new ArrayList<>(blocker);
        for(var item :domains.entrySet()){
            result.domains.put(item.getKey(),new HashSet<>(item.getValue()));
        }
        result.dnsRecords = new ArrayList<>(dnsRecords);
        result.servers = new ArrayList<>(servers);
        return result;
    }
}
