package org.kendar.pacts.filters;

import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "*")
public class PactFilter  implements FilteringClass {
    private final Logger logger;

    public PactFilter(LoggerBuilder loggerBuilder){
        logger = loggerBuilder.build(PactFilter.class);
        logger.info("Pact LOADED");
    }
    @Override
    public String getId() {
        return "org.kendar.pact.filters.PactFilter";
    }

    @HttpMethodFilter(
            phase = HttpFilterType.POST_CALL,
            pathAddress = "*",
            method = "*",
            id = "8000daa6-277f-77ec-9621-0242ac1afe002")
    public boolean replay(Request req, Response res) {
        //Find if there is a matching pact
        //Check if the pact is respected
        //If not return a 500 error
        return false;   //Actually just pass by
    }
}
