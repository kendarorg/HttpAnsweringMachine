package org.kendar.dns;

import java.util.regex.Pattern;

public class PatternItem {
    private Pattern compile;
    private final String ip;
    private String name;
    private final String matcher;

    public PatternItem(String dns, String ip) {
        if(dns.startsWith("@")){
            matcher = dns.substring(1);
            compile = Pattern.compile(matcher);
        }else{
            name = dns;
            matcher = dns;
        }
        this.ip = ip;
    }

    public boolean match(String domain){
        if(name!=null) return name.equalsIgnoreCase(domain);
        return compile.matcher(domain).matches();
    }

    public String getIp() {
        return ip;
    }

    public String getMatcher() {
        return matcher;
    }

    public String writeHostsLine() {
        if(compile!=null){
            return "#"+ip+" "+matcher;
        }else{
            return ip+" "+matcher;
        }
    }
}
