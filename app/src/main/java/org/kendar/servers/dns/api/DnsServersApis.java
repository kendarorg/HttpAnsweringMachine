package org.kendar.servers.dns.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.servers.dns.DnsServerDescriptor;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@HttpTypeFilter(hostAddress = "${localhost.name}",
        blocking = true)
public class DnsServersApis implements FilteringClass {
    ObjectMapper mapper = new ObjectMapper();
    private final DnsMultiResolver dnsMultiResolver;

    public DnsServersApis(DnsMultiResolver dnsMultiResolver){

        this.dnsMultiResolver = dnsMultiResolver;
    }

    @Override
    public String getId() {
        return "org.kendar.servers.dns.api.DnsApis";
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/dns/servers",
            method = "GET")
    public boolean getExtraServers(Request req, Response res) throws JsonProcessingException {
        var dnsServeres = dnsMultiResolver.getExtraServers();
        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(dnsServeres));
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/dns/servers/{ip}",
            method = "GET")
    public boolean getDnsServer(Request req, Response res) throws JsonProcessingException {
        var dnsServeres = dnsMultiResolver.getExtraServers();
        var ip = req.getPathParameter("ip");
        res.addHeader("Content-type", "application/json");
        for(var item:dnsServeres){
            if(item.getIp().equalsIgnoreCase(ip)){

                res.setResponseText(mapper.writeValueAsString(item));
                return false;
            }
        }
        res.setStatusCode(404);
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/dns/servers/{ip}",
            method = "DELETE")
    public boolean removeDnsServer(Request req, Response res) {
        var dnsServeres = dnsMultiResolver.getExtraServers();
        var id = (req.getPathParameter("ip"));
        res.addHeader("Content-type", "application/json");
        var newList = new ArrayList<DnsServerDescriptor>();
        for(var item:dnsServeres){
            if(item.getIp().equalsIgnoreCase(id)){continue;}
            newList.add(item);
        }
        dnsMultiResolver.setExtraServers(newList);
        res.setStatusCode(200);
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/dns/servers/{id}",
            method = "PUT")
    public boolean updateDnsServer(Request req, Response res) throws JsonProcessingException {
        var dnsServeres = dnsMultiResolver.getExtraServers();
        var ip = (req.getPathParameter("ip"));
        res.addHeader("Content-type", "application/json");
        var newList = new ArrayList<DnsServerDescriptor>();
        var newData = mapper.readValue((String)req.getRequestText(),DnsServerDescriptor.class);
        for(var item:dnsServeres){
            var clone = item.clone();
            if(!clone.getIp().equalsIgnoreCase(ip)){
                newList.add(clone);
                continue;
            }
            clone.setName(newData.getName());
            clone.setIp(newData.getIp());
            clone.setEnabled(newData.isEnabled());
            newList.add(clone);
        }
        dnsMultiResolver.setExtraServers(newList);
        res.setStatusCode(200);
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/dns/servers/swap/{ip1}/{ip2}",
            method = "PUT")
    public boolean swapDnsServer(Request req, Response res) {
        var dnsServeres = dnsMultiResolver.getExtraServers();
        var ip1 = (req.getPathParameter("ip1"));
        var ip2 = (req.getPathParameter("ip2"));
        res.addHeader("Content-type", "application/json");
        var newList = new ArrayList<DnsServerDescriptor>();
        var id1Index=-1;
        var id2Index = -1;
        for (int i = 0; i < dnsServeres.size(); i++) {
            var clone = dnsServeres.get(i).clone();
            if(dnsServeres.get(i).getIp().equalsIgnoreCase(ip1))id1Index=i;
            if(dnsServeres.get(i).getIp().equalsIgnoreCase(ip2))id2Index=i;
            newList.add(clone);
        }
        var id1Clone = dnsServeres.get(id1Index).clone();
        dnsServeres.set(id1Index,dnsServeres.get(id2Index));
        dnsServeres.set(id2Index,id1Clone);
        dnsMultiResolver.setExtraServers(newList);
        res.setStatusCode(200);
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/dns/servers",
            method = "POST")
    public boolean addDnsServer(Request req, Response res) throws JsonProcessingException {
        var dnsServeres = dnsMultiResolver.getExtraServers();
        var id = (req.getPathParameter("id"));
        res.addHeader("Content-type", "application/json");
        var newList = new ArrayList<DnsServerDescriptor>();
        var newData = mapper.readValue((String)req.getRequestText(),DnsServerDescriptor.class);

        for(var item:dnsServeres){
            newList.add( item.clone());
        }
        newList.add(newData);
        dnsMultiResolver.setExtraServers(newList);
        res.setStatusCode(200);
        return false;
    }
}
