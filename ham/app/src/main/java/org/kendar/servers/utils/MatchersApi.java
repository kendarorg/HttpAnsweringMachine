package org.kendar.servers.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.http.matchers.MatchersRegistry;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class MatchersApi implements FilteringClass {

    private MatchersRegistry matchersRegistry;

    public MatchersApi(MatchersRegistry matchersRegistry) {

        this.matchersRegistry = matchersRegistry;
    }

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/matchers",
            method = "GET")
    @HamDoc(
            tags = {"base/utils"},
            description = "Retrieve all matchers",
            responses = @HamResponse(
                    body = String[].class
            ))
    public void getDnsMappings(Request req, Response res) throws JsonProcessingException {
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(matchersRegistry.getAll()));
    }
}
