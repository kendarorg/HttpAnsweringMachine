package org.kendar.servers.dns.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.dns.configurations.DnsConfig;
import org.kendar.dns.configurations.ExtraDnsServer;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@HttpTypeFilter(hostAddress = "${localhost.name}",
        blocking = true)
public class DnsServersApis implements FilteringClass {
    ObjectMapper mapper = new ObjectMapper();
    private final JsonConfiguration configuration;

    public DnsServersApis(JsonConfiguration configuration){

        this.configuration = configuration;
    }

    @Override
    public String getId() {
        return "org.kendar.servers.dns.api.DnsApis";
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/dns/servers",
            method = "GET",id="1002a4b4-277d-11ec-9621-0242ac130002")
    public boolean getExtraServers(Request req, Response res) throws JsonProcessingException {
        var dnsServeres = configuration.getConfiguration(DnsConfig.class).getExtraServers();
        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(dnsServeres));
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/dns/servers/{id}",
            method = "GET",id="1003a4b4-277d-11ec-9621-0242ac130002")
    public boolean getDnsServer(Request req, Response res) throws JsonProcessingException {
        var dnsServers = configuration.getConfiguration(DnsConfig.class).getExtraServers();
        var name = req.getPathParameter("id");
        for(var item:dnsServers){
            if(item.getId().equalsIgnoreCase(name)){

                res.addHeader("Content-type", "application/json");
                res.setResponseText(mapper.writeValueAsString(item));
                return false;
            }
        }
        res.setStatusCode(404);
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/dns/servers/{id}",
            method = "DELETE",id="1004a4b4-277d-11ec-9621-0242ac130002")
    public boolean removeDnsServer(Request req, Response res) {
        var cloned = configuration.getConfiguration(DnsConfig.class).copy();
        var dnsServeres = cloned.getExtraServers();
        var name = (req.getPathParameter("id"));
        var newList = new ArrayList<ExtraDnsServer>();
        for(var item:dnsServeres){
            if(item.getId().equalsIgnoreCase(name)){continue;}
            newList.add(item);
        }
        cloned.setExtraServers(newList);
        configuration.setConfiguration(cloned);
        res.setStatusCode(200);
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/dns/servers/{id}",
            method = "PUT",id="1005a4b4-277d-11ec-9621-0242ac130002")
    public boolean updateDnsServer(Request req, Response res) throws JsonProcessingException {

        var cloned = configuration.getConfiguration(DnsConfig.class).copy();
        var dnsServers = cloned.getExtraServers();
        var ip = (req.getPathParameter("id"));
        var newList = new ArrayList<ExtraDnsServer>();
        var newData = mapper.readValue((String)req.getRequestText(),ExtraDnsServer.class);
        for(var item:dnsServers){
            var clone = item.copy();
            if(!clone.getId().equalsIgnoreCase(ip)){
                newList.add(clone);
                continue;
            }
            clone.setAddress(newData.getAddress());
            clone.setResolved(newData.getResolved());
            newList.add(clone);
        }
        cloned.setExtraServers(newList);
        configuration.setConfiguration(cloned);
        res.setStatusCode(200);
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/dns/servers/swap/{id1}/{id2}",
            method = "PUT",id="1006a4b4-277d-11ec-9621-0242ac130002")
    public boolean swapDnsServer(Request req, Response res) {
        var cloned = configuration.getConfiguration(DnsConfig.class).copy();
        var dnsServeres = cloned.getExtraServers();
        var name1 = (req.getPathParameter("id1"));
        var name2 = (req.getPathParameter("id2"));
        var id1Index=-1;
        var id2Index = -1;
        for (int i = 0; i < dnsServeres.size(); i++) {
            if(dnsServeres.get(i).getId().equalsIgnoreCase(name1))id1Index=i;
            if(dnsServeres.get(i).getId().equalsIgnoreCase(name2))id2Index=i;
        }
        var id1Clone = dnsServeres.get(id1Index).copy();
        dnsServeres.set(id1Index,dnsServeres.get(id2Index));
        dnsServeres.set(id2Index,id1Clone);
        configuration.setConfiguration(cloned);
        res.setStatusCode(200);
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/dns/servers",
            method = "POST",id="1007a4b5-277d-11ec-9621-0242ac130002")
    public boolean addDnsServer(Request req, Response res) throws Exception {
        var cloned = configuration.getConfiguration(DnsConfig.class).copy();
        var dnsServeres = cloned.getExtraServers();
        var newList = new ArrayList<ExtraDnsServer>();
        var newData = mapper.readValue((String)req.getRequestText(),ExtraDnsServer.class);

        for(var item:dnsServeres){
            if(item.getId().equalsIgnoreCase(newData.getId()))throw new Exception("Duplicate dns resolution");
            newList.add( item.copy());
        }
        newList.add(newData);
        cloned.setExtraServers(newList);
        configuration.setConfiguration(cloned);
        res.setStatusCode(200);
        return false;
    }
}
