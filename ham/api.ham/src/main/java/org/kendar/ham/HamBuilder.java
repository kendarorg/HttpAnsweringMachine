package org.kendar.ham;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class HamBuilder implements HamBasicBuilder{

    String host;
    Integer port;
    String protocol;

    private HamBuilder(){}
    public static HamBasicBuilder newHam(String host){
        var result = new HamBuilder();
        result.host = host;
        result.port = null;
        result.protocol = "http";
        return result;
    }

    public HamBasicBuilder withPort(int port){
        this.port = port;
        return this;
    }

    public HamBasicBuilder withHttps(){
        this.protocol = "https";
        return this;
    }

    public DnsBuilder dns(){
        return new DnsBuilderImpl(this);
    }

    public HamRequestBuilder newRequest(){
        var result = HamRequestBuilder.newRequest(protocol,host);
        if(port!=null)result.withPort(port);
        return result;
    }

    ObjectMapper mapper = new ObjectMapper();

    public Response call(Request request){
        return null;
    }

    public Response expectCode(Response response,int code,Supplier<String> getExceptionMessage) throws HamException {
        if(response.getStatusCode()!=code){
            throw new HamException(getExceptionMessage.get());
        }
        return response;
    }

    public Response expectCode(Response response,int code,String getExceptionMessage) throws HamException {
        if(response.getStatusCode()!=code){
            throw new HamException(getExceptionMessage);
        }
        return response;
    }
    public <T> T callJson(Request request,Class<T> clazz) throws HamException {
        try {
            return (T)mapper.readValue(request.getRequestText(),clazz);
        } catch (JsonProcessingException e) {
            throw new HamException(e);
        }
    }


    public <T> List<T> callJsonList(Request request,Class<T> clazz) throws HamException {
        try {
            return (List<T>)mapper.readValue(request.getRequestText(), new TypeReference<List<T>>() {
            });
        } catch (JsonProcessingException e) {
            throw new HamException(e);
        }
    }

    public static String updateMethod(Optional val){
        return val.isPresent()?"PUT":"POST";
    }
    public static String pathId(String path,Optional val, Supplier<String> idSupplier){
        return path+(val.isPresent()?"/"+idSupplier.get():"");
    }
    public static void queryId(Request request,Optional val, String name, Supplier<String> idSupplier){
        if(val.isPresent()){
            request.addQuery(name,idSupplier.get());
        }
    }

    public CertificatesBuilder certificates(){
        return new CertificatesBuilderImpl(this);
    }
    public ProxyBuilder proxyes(){
        return new ProxyBuilderImpl(this);
    }
    public <T extends Object> T plugin(Class<T> clazz){
        return null;
    }
}
