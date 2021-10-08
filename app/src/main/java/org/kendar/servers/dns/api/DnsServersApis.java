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
            method = "GET",id="1002a4b4-277d-11ec-9621-0242ac130002")
    public boolean getExtraServers(Request req, Response res) throws JsonProcessingException {
        var dnsServeres = dnsMultiResolver.getExtraServers();
        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(dnsServeres));
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/dns/servers/{name}",
            method = "GET",id="1003a4b4-277d-11ec-9621-0242ac130002")
    public boolean getDnsServer(Request req, Response res) throws JsonProcessingException {
        var dnsServers = dnsMultiResolver.getExtraServers();
        var name = req.getPathParameter("name");
        res.addHeader("Content-type", "application/json");
        for(var item:dnsServers){
            if(item.getName().equalsIgnoreCase(name)){

                res.setResponseText(mapper.writeValueAsString(item));
                return false;
            }
        }
        res.setStatusCode(404);
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/dns/servers/{name}",
            method = "DELETE",id="1004a4b4-277d-11ec-9621-0242ac130002")
    public boolean removeDnsServer(Request req, Response res) {
        var dnsServeres = dnsMultiResolver.getExtraServers();
        var name = (req.getPathParameter("name"));
        res.addHeader("Content-type", "application/json");
        var newList = new ArrayList<DnsServerDescriptor>();
        for(var item:dnsServeres){
            if(item.getName().equalsIgnoreCase(name)){continue;}
            newList.add(item);
        }
        dnsMultiResolver.setExtraServers(newList);
        res.setStatusCode(200);
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/dns/servers/{name}",
            method = "PUT",id="1005a4b4-277d-11ec-9621-0242ac130002")
    public boolean updateDnsServer(Request req, Response res) throws JsonProcessingException {
        var dnsServers = dnsMultiResolver.getExtraServers();
        var ip = (req.getPathParameter("name"));
        res.addHeader("Content-type", "application/json");
        var newList = new ArrayList<DnsServerDescriptor>();
        var newData = mapper.readValue((String)req.getRequestText(),DnsServerDescriptor.class);
        for(var item:dnsServers){
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
            pathAddress = "/api/dns/servers/swap/{name1}/{name2}",
            method = "PUT",id="1006a4b4-277d-11ec-9621-0242ac130002")
    public boolean swapDnsServer(Request req, Response res) {
        var dnsServeres = dnsMultiResolver.getExtraServers();
        var name1 = (req.getPathParameter("name1"));
        var name2 = (req.getPathParameter("name2"));
        res.addHeader("Content-type", "application/json");
        var newList = new ArrayList<DnsServerDescriptor>();
        var id1Index=-1;
        var id2Index = -1;
        for (int i = 0; i < dnsServeres.size(); i++) {
            var clone = dnsServeres.get(i).clone();
            if(dnsServeres.get(i).getName().equalsIgnoreCase(name1))id1Index=i;
            if(dnsServeres.get(i).getName().equalsIgnoreCase(name2))id2Index=i;
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
            method = "POST",id="1007a4b5-277d-11ec-9621-0242ac130002")
    public boolean addDnsServer(Request req, Response res) throws Exception {
        var dnsServeres = dnsMultiResolver.getExtraServers();
        res.addHeader("Content-type", "application/json");
        var newList = new ArrayList<DnsServerDescriptor>();
        var newData = mapper.readValue((String)req.getRequestText(),DnsServerDescriptor.class);

        for(var item:dnsServeres){
            if(item.getName().equalsIgnoreCase(newData.getName()))throw new Exception("Duplicate dns resolution");
            newList.add( item.clone());
        }
        newList.add(newData);
        dnsMultiResolver.setExtraServers(newList);
        res.setStatusCode(200);
        return false;
    }
}
