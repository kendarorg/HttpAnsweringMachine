package org.kendar.ham;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class HamBuilder {

    String host;
    Integer port;
    String protocol;

    private HamBuilder(){}
    public static HamBuilder newHam(String host){
        var result = new HamBuilder();
        result.host = host;
        result.port = null;
        result.protocol = "http";
        return result;
    }

    public HamBuilder withPort(int port){
        this.port = port;
        return this;
    }

    public HamBuilder withHttps(){
        this.protocol = "https";
        return this;
    }

    public DnsBuilder dns(){
        return new DnsBuilderImpl(this);
    }

    HamRequestBuilder newRequest(){
        var result = HamRequestBuilder.newRequest(protocol,host);
        if(port!=null)result.withPort(port);
        return result;
    }

    ObjectMapper mapper = new ObjectMapper();

    Response call(Request request){
        return null;
    }

    Response expectCode(Response response,int code,Supplier<String> getExceptionMessage) throws HamException {
        if(response.getStatusCode()!=code){
            throw new HamException(getExceptionMessage.get());
        }
        return response;
    }

    Response expectCode(Response response,int code,String getExceptionMessage) throws HamException {
        if(response.getStatusCode()!=code){
            throw new HamException(getExceptionMessage);
        }
        return response;
    }
    <T> T callJson(Request request,Class<T> clazz) throws HamException {
        try {
            return (T)mapper.readValue(request.getRequestText(),clazz);
        } catch (JsonProcessingException e) {
            throw new HamException(e);
        }
    }


    <T> List<T> callJsonList(Request request,Class<T> clazz) throws HamException {
        try {
            return (List<T>)mapper.readValue(request.getRequestText(), new TypeReference<List<T>>() {
            });
        } catch (JsonProcessingException e) {
            throw new HamException(e);
        }
    }

    static String updateMethod(Optional val){
        return val.isPresent()?"PUT":"POST";
    }
    static String pathId(String path,Optional val, Supplier<String> idSupplier){
        return path+(val.isPresent()?"/"+idSupplier.get():"");
    }
    static void queryId(Request request,Optional val, String name, Supplier<String> idSupplier){
        if(val.isPresent()){
            request.addQuery(name,idSupplier.get());
        }
    }

    public CertificatesBuilder certificates(){
        return new CertificatesBuilderImpl(this);
    }
}
