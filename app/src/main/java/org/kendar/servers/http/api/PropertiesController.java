package org.kendar.servers.http.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}",
        blocking = true)
public class PropertiesController implements FilteringClass {
    private final Environment environment;
    ObjectMapper mapper = new ObjectMapper();

    public PropertiesController(Environment environment){

        this.environment = environment;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/properties",
            method = "GET",id="1021a4b4-277d-11ec-9621-0242ac130002")
    public boolean getProperties(Request req, Response res) throws JsonProcessingException {

        List<String> result = new ArrayList<>();
        Map<String, String> map = new HashMap();
        for(Iterator it = ((AbstractEnvironment) environment).getPropertySources().iterator(); it.hasNext(); ) {
            PropertySource propertySource = (PropertySource) it.next();
            if(!propertySource.getName().startsWith("loaded.from."))continue;
            if (propertySource instanceof MapPropertySource) {
                for(var kvp:((MapPropertySource) propertySource).getSource().entrySet()){
                    map.put(kvp.getKey(),kvp.getValue().toString());
                    result.add(kvp.getKey()+"="+kvp.getValue().toString());
                }
            }
        }
        if(req.getQuery("asfile")!=null){
            Collections.sort(result);
            res.addHeader("Content-type", "text/plain");
            res.setResponseText(String.join("\n",result));
        }else {
            res.addHeader("Content-type", "application/json");
            res.setResponseText(mapper.writeValueAsString(map));
        }
        return true;
    }

    @Override
    public String getId() {
        return "org.kendar.servers.http.api.PropertiesController";
    }
}
