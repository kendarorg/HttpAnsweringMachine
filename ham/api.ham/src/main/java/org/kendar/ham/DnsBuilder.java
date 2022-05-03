package org.kendar.ham;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface DnsBuilder {
    public class DnsName {
        public String id;
        public String ip;
        public String dns;
    }
    public class ResolvedNames {
        public String ip;
        public String name;
    }
    public class DnsServer {
        public String id;
        public String address;
        public boolean enabled;
    }
    String addDnsName(String ip, String name) throws HamException;
    void removeDnsName(String id);
    List<DnsName> retrieveDnsNames() throws JsonProcessingException, HamException;
    String addDnsServer(String address,boolean enabled) throws HamException;
    void removeDnsServer(String id);
    List<DnsServer> retrieveDnsServers() throws HamException;
    List<ResolvedNames> retrieveResolvedNames() throws HamException;
}
