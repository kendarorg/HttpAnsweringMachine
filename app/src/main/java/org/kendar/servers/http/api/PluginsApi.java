package org.kendar.servers.http.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.http.PluginsAddressesRecorder;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.http.api.model.PluginDescriptor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@HttpTypeFilter(hostAddress = "${localhost.name}",
        blocking = true)
public class PluginsApi implements FilteringClass {
    ObjectMapper mapper = new ObjectMapper();
    private PluginsAddressesRecorder pluginsAddressesRecorder;

    public PluginsApi(PluginsAddressesRecorder pluginsAddressesRecorder){

        this.pluginsAddressesRecorder = pluginsAddressesRecorder;
    }

    @Override
    public String getId() {
        return "org.kendar.servers.http.api.PluginsApi";
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/plugins",
            method = "GET",id="1zty7a4b4-277d-11ec-9621-0242ac130002")
    public boolean getStatus(Request req, Response res) throws JsonProcessingException {
        var result = new ArrayList<PluginDescriptor>();
        for(var item : pluginsAddressesRecorder.getPluginAddresses().entrySet()){
            result.add(new PluginDescriptor(item.getKey(),item.getValue()));
        }

        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(result));
        return false;
    }
}
