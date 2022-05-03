package org.kendar.ham;

import org.kendar.servers.http.Response;

import java.io.IOException;
import java.io.InputStream;

public class HamResponseBuilder {

    private HamResponseBuilder(){}
    private Response response;
    public static HamResponseBuilder newResponse(){
        var result = new HamResponseBuilder();
        result.response = new Response();
        return result;
    }

    public HamResponseBuilder withHeader(String id, String value){
        response.addHeader(id,value);
        return this;
    }

    public HamResponseBuilder withContentType(String value){
        return withHeader("content-type",value);
    }


    public HamResponseBuilder withText(String text){
        response.setResponseText(text);
        response.setBinaryResponse(false);
        return this;
    }
    public HamResponseBuilder withStatus(int statusCode){
        response.setStatusCode(statusCode);
        return this;
    }

    public HamResponseBuilder withText(InputStream text) throws IOException {
        response.setResponseText(new String(text.readAllBytes()));
        response.setBinaryResponse(false);
        return this;
    }


    public HamResponseBuilder withBytes(byte[] data){
        response.setResponseBytes(data);
        response.setBinaryResponse(true);
        return this;
    }


    public HamResponseBuilder withBytes(InputStream data) throws IOException {
        response.setResponseBytes(data.readAllBytes());
        response.setBinaryResponse(true);
        return this;
    }

    public Response build(){
        return response;
    }
}
