package org.kendar.servers.dns.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.Example;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@HttpTypeFilter(hostAddress = "*", blocking = true)
public class DnsLookupApi implements FilteringClass {
    private final DnsMultiResolver dnsMultiResolver;
    private final ObjectMapper mapper = new ObjectMapper();

    public DnsLookupApi(DnsMultiResolver dnsMultiResolver) {

        this.dnsMultiResolver = dnsMultiResolver;
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/dns/lookup/{id}",
            method = "GET")
    @HamDoc(
            tags = {"base/utils/lookup"},
            description = "Lookup DNSs via http",
            path = @PathParameter(
                    key = "id",
                    description = "Host name",
                    example = "www.kendar.org"),
            responses = @HamResponse(
                    body = String.class,
                    examples = {
                            @Example(example = "192.168.1.1")
                    }
            ))
    public void resolve(Request req, Response res) throws Exception {
        var toResolve = req.getPathParameter("id");
        var resultList = dnsMultiResolver.resolve(toResolve);
        if (resultList.size() > 0) {
            res.setResponseText(resultList.get(0));
        } else {
            res.setResponseText("");
        }
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.TEXT);
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/dns/list",
            method = "GET")
    @HamDoc(
            tags = {"base/utils/lookup"},
            description = "List all resolved dnss",
            responses = @HamResponse(
                    body = DnsItem[].class
            ))
    public void listAll(Request req, Response res) throws Exception {
        var resultList = dnsMultiResolver.listDomains();
        var result = new ArrayList<DnsItem>();
        for (var item : resultList.entrySet()) {
            var ni = new DnsItem();
            ni.setName(item.getKey());
            ni.setIp(item.getValue());
            result.add(ni);
        }
        res.setResponseText(mapper.writeValueAsString(result));
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/dns/list",
            method = "DELETE")
    @HamDoc(
            tags = {"base/utils/lookup"}, description = "Force the dns resolved reloading")
    public void clear(Request req, Response res) throws Exception {
        dnsMultiResolver.clearCache();
    }
}
