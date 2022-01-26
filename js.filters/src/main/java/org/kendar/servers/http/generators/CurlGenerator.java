package org.kendar.servers.http.generators;

import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class CurlGenerator implements BaseGenerator{
    @Override
    public String getType() {
        return "curl";
    }

    @Override
    public String getDescription() {
        return "Generate a bash file calling the API with curl";
    }

    @Override
    public String generate(Request request, Response response) {
        var result = new ArrayList<String>();
        result.add("#!/bin/sh");
        result.add("curl https://www.google.com");
        result.add("");
        return String.join("\n",result);
    }
}
