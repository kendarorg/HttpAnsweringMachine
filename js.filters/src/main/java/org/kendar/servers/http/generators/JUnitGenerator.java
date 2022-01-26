package org.kendar.servers.http.generators;

import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

@Component
public class JUnitGenerator implements BaseGenerator{
    @Override
    public String getType() {
        return "junit";
    }

    @Override
    public String getDescription() {
        return "Generate a Junit test with apache client to verifiy the match with the request";
    }

    @Override
    public String generate(Request request, Response response) {
        return null;
    }
}
