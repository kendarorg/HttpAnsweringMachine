package org.kendar.servers.dns.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.dns.DnsMultiResolverImpl;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${localhost.name}",
        blocking = true)
public class DnsMappingsApis implements FilteringClass {
    private final DnsMultiResolverImpl dnsMultiResolver;
    ObjectMapper mapper = new ObjectMapper();

    public DnsMappingsApis(DnsMultiResolverImpl dnsMultiResolver) {

        this.dnsMultiResolver = dnsMultiResolver;
    }

    @Override
    public String getId() {
        return "org.kendar.servers.dns.api.DnsApis";
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/dns/mappings",
            method = "GET",id="1000a4b4-277d-11ec-9621-0242ac130002")
    public boolean getDnsMappings(Request req, Response res) throws JsonProcessingException {
        var records = dnsMultiResolver.getDnsRecords();
        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(records));
        return false;
    }
}
