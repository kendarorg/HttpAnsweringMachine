package org.kendar.servers.certificates.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.http.configurations.CertificatesSSLConfig;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@HttpTypeFilter(hostAddress = "${localhost.name}",
        blocking = true)
public class SSLController implements FilteringClass {
    ObjectMapper mapper = new ObjectMapper();
    private CertificatesSSLConfig answeringHttpsServer;

    public SSLController(CertificatesSSLConfig applicationContext){

        this.answeringHttpsServer = applicationContext;
    }


    @Override
    public String getId() {
        return "org.kendar.servers.certificates.api.SSLController";
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/ssl",
            method = "GET",id="1008a4b4-277d-11ec-9621-0242ac130002")
    public boolean getExtraServers(Request req, Response res) throws JsonProcessingException {
        var dnsServeres = answeringHttpsServer.get().extraDomains;
        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(dnsServeres));
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/ssl/{name}",
            method = "DELETE",id="1009a4b4-277d-11ec-9621-0242ac130002")
    public boolean removeDnsServer(Request req, Response res) {
        var cloned = answeringHttpsServer.get().copy();
        var dnsServeres = cloned.extraDomains;
        var name = req.getPathParameter("name");

        var newList = new ArrayList<String>();
        for(var item:dnsServeres){
            if(item.equalsIgnoreCase(name)){continue;}
            newList.add(item);
        }
        cloned.extraDomains = newList;
        answeringHttpsServer.set(cloned);
        res.setStatusCode(200);
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/ssl/swap/{name1}/{name2}",
            method = "PUT",id="1010a4b4-277d-11ec-9621-0242ac130002")
    public boolean swapDnsServer(Request req, Response res) {
        var cloned = answeringHttpsServer.get().copy();
        var dnsServeres = cloned.extraDomains;
        var name1 = (req.getPathParameter("name1"));
        var name2 = (req.getPathParameter("name2"));

        var newList = new ArrayList<String>();
        var id1Index=-1;
        var id2Index = -1;
        for (int i = 0; i < dnsServeres.size(); i++) {
            var clone = dnsServeres.get(i)+"";
            if(dnsServeres.get(i).equalsIgnoreCase(name1))id1Index=i;
            if(dnsServeres.get(i).equalsIgnoreCase(name2))id2Index=i;
            newList.add(clone);
        }
        var id1Clone = dnsServeres.get(id1Index)+"";
        dnsServeres.set(id1Index,dnsServeres.get(id2Index));
        dnsServeres.set(id2Index,id1Clone);
        cloned.extraDomains = newList;
        answeringHttpsServer.set(cloned);
        res.setStatusCode(200);
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/ssl/{name}",
            method = "POST",id="1011a4b4-277d-11ec-9621-0242ac130002")
    public boolean addDnsServer(Request req, Response res) throws Exception {
        var cloned = answeringHttpsServer.get().copy();
        var dnsServeres = cloned.extraDomains;
        var newData = req.getPathParameter("name");

        var newList = new ArrayList<String>();
        for(var item:dnsServeres){
            if(item.equalsIgnoreCase(newData))throw new Exception("Duplicate dns resolution");
            newList.add( item);
        }
        newList.add(newData);
        cloned.extraDomains = newList;
        answeringHttpsServer.set(cloned);
        res.setStatusCode(200);
        return false;
    }
}
