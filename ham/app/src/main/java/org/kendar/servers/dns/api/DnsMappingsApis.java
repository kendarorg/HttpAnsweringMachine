package org.kendar.servers.dns.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.dns.configurations.DnsConfig;
import org.kendar.dns.configurations.PatternItem;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}",
        blocking = true)
public class DnsMappingsApis implements FilteringClass {
    private final JsonConfiguration configuration;
    final ObjectMapper mapper = new ObjectMapper();

    public DnsMappingsApis(JsonConfiguration configuration) {

        this.configuration = configuration;
    }

    @Override
    public String getId() {
        return "org.kendar.servers.dns.api.DnsApis";
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/dns/mappings",
            method = "GET",id="1000a4b4-277d-11ef-9621-0242ac130002")
    public void getDnsMappings(Request req, Response res) throws JsonProcessingException {
        var records = configuration.getConfiguration(DnsConfig.class).getResolved();
        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(records));
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/dns/hosts",
            method = "GET",id="1000a4b4asd-277d-11ef-9621-0242ac130002")
    public void getHostsFile(Request req, Response res) throws JsonProcessingException {
        var records = configuration.getConfiguration(DnsConfig.class).getResolved();
        var result = "";
        for(var record:records){
            if(!record.patternMatcher()){
                result+=record.getIp()+" "+record.getDns()+"\r\n";
            }
        }
        res.addHeader("Content-type", "text/plain");
        res.addHeader("Content-disposition", "inline;filename=hosts");

        res.setResponseText(result);
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
      pathAddress = "/api/dns/mappings/{id}",
      method = "PUT",id="1000a4f4-277d-11ef-9621-0242ac130002")
    public void saveDnsMappings(Request req, Response res) throws JsonProcessingException {
        var id = req.getPathParameter("id");
        var newObject = mapper.readValue(req.getRequestText(), PatternItem.class);
        newObject.initialize();
        var dnsConfig = configuration.getConfiguration(DnsConfig.class).copy();
        var newMapped = new ArrayList<PatternItem>();
        for(var config :dnsConfig.getResolved()){
            if(config.getId().equalsIgnoreCase(id)) continue;
            newMapped.add(config);
        }
        newMapped.add(newObject);
        dnsConfig.setResolved(newMapped);
        configuration.setConfiguration(dnsConfig);
        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(newMapped));
    }



    @HttpMethodFilter(phase = HttpFilterType.API,
      pathAddress = "/api/dns/mappings",
      method = "POST",id="3000a4f4-277k-11ef-9621-0242ac130002")
    public void addDnsMappings(Request req, Response res) throws Exception {

        var cloned = configuration.getConfiguration(DnsConfig.class).copy();

        List<PatternItem> newList = new ArrayList<>();
        if(req.getRequestText().startsWith("[")){
            newList = (List<PatternItem>)mapper.readValue(req.getRequestText(), List.class).stream()
                    .map(a->{
                        var newDomain = new PatternItem();
                        newDomain.setId(UUID.randomUUID().toString());
                        newDomain.setIp("127.0.0.1");
                        newDomain.setDns((String)a);
                        newDomain.initialize();
                        return newDomain;
                    }).collect(Collectors.toList());

        }else {
            var newITem = mapper.readValue(req.getRequestText(), PatternItem.class);
            newITem.initialize();
            newList.add( newITem);
        }

        final var newList2= newList;
        var notNew = cloned.getResolved().stream()
                .filter(a->!newList2.stream().anyMatch(m-> m.getDns().equalsIgnoreCase(a.getDns())))
                .collect(Collectors.toList());
        newList.addAll(notNew);

        cloned.setResolved(newList);
        configuration.setConfiguration(cloned);
        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(cloned));
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
      pathAddress = "/api/dns/mappings/{id}",
      method = "DELETE",id="10k0a4f4-277d-11ef-9621-0242ac130002")
    public void deleteDnsMappings(Request req, Response res) throws JsonProcessingException {
        var id = req.getPathParameter("id");
        var dnsConfig = configuration.getConfiguration(DnsConfig.class).copy();
        var newMapped = new ArrayList<PatternItem>();
        for(var config :dnsConfig.getResolved()){
            if(config.getId().equalsIgnoreCase(id)) continue;
            newMapped.add(config);
        }
        dnsConfig.setResolved(newMapped);
        configuration.setConfiguration(dnsConfig);
        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(newMapped));
    }
}
