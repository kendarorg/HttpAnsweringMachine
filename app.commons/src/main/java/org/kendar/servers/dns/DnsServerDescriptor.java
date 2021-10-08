package org.kendar.servers.dns;

public class DnsServerDescriptor {
    private String ip;
    private String name;
    private boolean enabled =true;

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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public DnsServerDescriptor copy(){
        var result = new DnsServerDescriptor();
        result.setEnabled(this.isEnabled());
        result.setIp(this.getIp());
        result.setName(this.getName());
        return result;
    }
}
