package org.kendar.dns.configuration;

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
}
