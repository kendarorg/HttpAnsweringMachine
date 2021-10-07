package org.kendar.servers.http.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.http.api.model.FilterDto;
import org.kendar.servers.http.configurations.FilterConfig;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Locale;

@Component
@HttpTypeFilter(hostAddress = "${localhost.name}",
        blocking = true)
public class FilterClassesApi  implements FilteringClass {
    private FilterConfig filteringClassesHandler;
    private Environment environment;

    public FilterClassesApi(FilterConfig filtersConfiguration, Environment environment){

        this.filteringClassesHandler = filtersConfiguration;
        this.environment = environment;
    }


    ObjectMapper mapper = new ObjectMapper();
    public class FilterType{
        public FilterType(int index,HttpFilterType type){

            this.index = index;
            this.type = type;
        }
        private HttpFilterType type;
        private int index;

        public HttpFilterType getType() {
            return type;
        }

        public void setType(HttpFilterType type) {
            this.type = type;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }

    @Override
    public String getId() {
        return "org.kendar.servers.http.api.FilterClassesApi";
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/filters",
            method = "GET")
    public void listPhases(Request req, Response res) throws JsonProcessingException {
        var result = new ArrayList<FilterType>();
        result.add(new FilterType(0,HttpFilterType.NONE));
        result.add(new FilterType(1,HttpFilterType.PRE_RENDER));
        result.add(new FilterType(2,HttpFilterType.API));
        result.add(new FilterType(3,HttpFilterType.STATIC));
        result.add(new FilterType(4,HttpFilterType.PRE_CALL));
        result.add(new FilterType(5,HttpFilterType.POST_CALL));
        result.add(new FilterType(6,HttpFilterType.POST_RENDER));
        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(result));
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/filters/{phase}",
            method = "GET")
    public void getFiltersForPhase(Request req, Response res) throws JsonProcessingException {
        var stringPhase = req.getPathParameter("phase");
        var phase = HttpFilterType.valueOf(stringPhase.toUpperCase(Locale.ROOT));
        var config = filteringClassesHandler.get();
        var result = new ArrayList<FilterDto>();
        var listOfItems = config.filters.get(phase);
        for(var i=0;i<listOfItems.size();i++){
            var item = listOfItems.get(i);
            var desc = new FilterDto(i,item.getId(),item.getTypeFilter(),item.getMethodFilter());
            result.add(desc);
        }

        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(result));
    }
}
