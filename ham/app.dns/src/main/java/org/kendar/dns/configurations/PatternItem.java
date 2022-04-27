package org.kendar.dns.configurations;

import org.kendar.servers.Copyable;

import java.util.regex.Pattern;

public class PatternItem implements Copyable<PatternItem> {
    private String id;
    private String dns;
    private Pattern compile;
    private String ip;
    private String name;
    private  String matcher;
    private boolean initialized = false;

    public void initialize(){
        if(!initialized){
            if(dns.startsWith("@")){
                matcher = dns.substring(1);
                compile = Pattern.compile(matcher);
            }else{
                name = dns;
                matcher = dns;
            }
            initialized = true;
        }
    }
    public PatternItem(){

    }

    public PatternItem(String id,String dns, String ip) {
        this.id=id;
        this.dns = dns;
        this.ip = ip;
        initialize();
    }

    public boolean match(String domain){
        initialize();
        if(name!=null) return name.equalsIgnoreCase(domain);
        return compile.matcher(domain).matches();
    }

    public boolean patternMatcher(){
        return name==null || name.isEmpty();
    }

    public String getIp() {
        return ip;
    }

    public String writeHostsLine() {
        if(compile!=null){
            return "#"+ip+" "+matcher;
        }else{
            return ip+" "+matcher;
        }
    }

    public String getDns() {
        return dns;
    }

    public void setDns(String dns) {
        initialized = false;
        this.dns = dns;
    }

    public void setIp(String ip) {
        initialized = false;
        this.ip = ip;
    }

    public PatternItem copy() {
        return new PatternItem(id,dns,ip);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
