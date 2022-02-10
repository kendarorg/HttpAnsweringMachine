package org.kendar.servers.dns.api;

import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "*", blocking = true)
public class DnsLookupApi implements FilteringClass {
    private DnsMultiResolver dnsMultiResolver;

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
}
