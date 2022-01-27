package org.kendar.servers.http.generators;

import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

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

        var dataFile = UUID.randomUUID() +".data";
        if(!request.isBinaryRequest()){
            var base64Bytes = new String(Base64.getEncoder().encode(request.getRequestBytes()));
            result.add("echo '"+base64Bytes+"' |base64 -d -o "+dataFile);
        }

        var target = request.getProtocol()+"://"+request.getHost()+":"+request.getPort()+"/"+request.getPath();
        target+="?"+ request.getQuery().entrySet().stream().map(e->e.getKey()+"="+e.getValue()).collect(Collectors.joining("&"));
        result.add("curl -X "+request.getMethod().toUpperCase(Locale.ROOT)+"\\");
        for (var head :
                request.getHeaders().entrySet()){
            result.add("\t-H '"+head.getKey()+":"+head.getValue()+"'\\");
        }
        if((request.getRequestBytes()!=null &&request.getRequestBytes().length>0 ) ||
                (request.getRequestText()!=null && request.getRequestText().length()>0)){
            result.add("\t-F 'file=@/"+dataFile+"'\\");
        }
        for (var postParameter :
                request.getPostParameters().entrySet()){
            result.add("\t-F \""+postParameter.getKey()+"="+postParameter.getValue()+"\"\\");
        }
        result.add("\t"+target);


        result.add("");
        return String.join("\n",result);
    }
}
