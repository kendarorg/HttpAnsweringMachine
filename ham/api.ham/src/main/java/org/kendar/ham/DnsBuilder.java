package org.kendar.ham;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

/**
 * To contact the DNS APIs
 */
public interface DnsBuilder {
    /**
     * Resolve the given dns name.
     * @param domain
     * @return the IP string
     * @throws HamException
     */
    String resolve(String domain) throws HamException;

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

    /**
     * Add a new DNS resolution
     * @param ip the ip to which the name will be resolved
     * @param name the name resolved
     * @return the id of the generated name
     * @throws HamException
     */
    String addDnsName(String ip, String name) throws HamException;

    /**
     * Remove an id by name
     * @param id
     * @throws HamException
     */
    void removeDnsName(String id) throws HamException;

    /**
     * Retrieve all registered DNS ip-name associations
     * @return
     * @throws HamException
     */
    List<DnsName> retrieveDnsNames() throws HamException;

    /**
     * Add a new DNS server
     * @param address the ip or dns name of the server
     * @param enabled if should be enabled at start
     * @return
     * @throws HamException
     */
    String addDnsServer(String address,boolean enabled) throws HamException;

    /**
     * Remove DNS server by id
     * @param id
     * @throws HamException
     */
    void removeDnsServer(String id) throws HamException;

    /**
     * Retrieves all DNS servers
     * @return
     * @throws HamException
     */
    List<DnsServer> retrieveDnsServers() throws HamException;

    /**
     * Retrieve all the DNS resolved through ham dns server
     * @return
     * @throws HamException
     */
    List<ResolvedNames> retrieveResolvedNames() throws HamException;
}
