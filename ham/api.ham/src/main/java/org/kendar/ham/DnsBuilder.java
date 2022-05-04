package org.kendar.ham;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface DnsBuilder {
    String resolve(String s) throws HamException;

    public class DnsName {
        private String id;
        private String ip;
        private String dns;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getDns() {
            return dns;
        }

        public void setDns(String dns) {
            this.dns = dns;
        }
    }
    public class ResolvedNames {
        private String ip;
        private String name;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    public class DnsServer {
        private String id;
        private String address;

        private String resolved;
        private boolean enabled;

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

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getResolved() {
            return resolved;
        }

        public void setResolved(String resolved) {
            this.resolved = resolved;
        }
    }
    String addDnsName(String ip, String name) throws HamException;
    void removeDnsName(String id) throws HamException;
    List<DnsName> retrieveDnsNames() throws HamException;
    String addDnsServer(String address,boolean enabled) throws HamException;
    void removeDnsServer(String id) throws HamException;
    List<DnsServer> retrieveDnsServers() throws HamException;
    List<ResolvedNames> retrieveResolvedNames() throws HamException;
}
