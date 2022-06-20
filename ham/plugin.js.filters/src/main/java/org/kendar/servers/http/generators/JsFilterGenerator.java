package org.kendar.servers.http.generators;

import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class JsFilterGenerator implements BaseGenerator{
    @Override
    public String getType() {
        return "jsfilter";
    }

    @Override
    public String getDescription() {
        return "Generate a json filter file that will verify that the call is matching with what was recorded";
    }

    @Override
    public String generate(Request request, Response response) {
        var result = new ArrayList<String>();
        result.add("var continue = response.responseText!=\"Nothing\";");
        result.add("if(!continue)utils.handleEvent('PactViolation','{}');");
        result.add("var result ={");
        result.add("    request:request,");
        result.add("    response:response,");
        result.add("    continue:false");
        result.add("};");
        result.add("return result;");
        result.add("");
        return String.join("\n",result);
    }
}
