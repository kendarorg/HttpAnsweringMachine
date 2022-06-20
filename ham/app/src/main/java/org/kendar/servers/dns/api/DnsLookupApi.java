package org.kendar.servers.dns.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@HttpTypeFilter(hostAddress = "*", blocking = true)
public class DnsLookupApi implements FilteringClass {
    private final DnsMultiResolver dnsMultiResolver;
    private final ObjectMapper mapper = new ObjectMapper();
    public DnsLookupApi(DnsMultiResolver dnsMultiResolver){

        this.dnsMultiResolver = dnsMultiResolver;
    }
    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/dns/lookup/{id}",
            method = "GET",
            id = "1005a4b91277d-11ec-9621-0242ac130002")
    public void resolve(Request req, Response res) throws Exception {
        var toResolve = req.getPathParameter("id");
        var resultList = dnsMultiResolver.resolve(toResolve);
        if(resultList.size()>0){
            res.setResponseText(resultList.get(0));
        }else{
            res.setResponseText("");
        }
        res.addHeader("content-type","text/plain");
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/dns/list",
            method = "GET",
            id = "1005a4b91277d-11ec-9621-0fdns130002")
    public void listAll(Request req, Response res) throws Exception {
        var resultList = dnsMultiResolver.listDomains();
        var result = new ArrayList<DnsItem>();
        for(var item:resultList.entrySet()){
            var ni = new DnsItem();
            ni.setName(item.getKey());
            ni.setIp(item.getValue());
            result.add(ni);
        }
        res.setResponseText(mapper.writeValueAsString(result));
        res.addHeader("content-type","application/json");
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/dns/list",
            method = "DELETE",
            id = "1005a4b919877d-11ec-9621-0fdns130002")
    public void clear(Request req, Response res) throws Exception {
        dnsMultiResolver.clearCache();
    }
}
