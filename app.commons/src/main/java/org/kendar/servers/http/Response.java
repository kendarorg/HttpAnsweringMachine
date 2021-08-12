package org.kendar.servers.http;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.kendar.utils.MimeChecker;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Response {
    private HashMap<String,String> headers = new HashMap<>();
    private boolean binaryResponse =false;
    private byte[] responseBytes;
    private String responseText;
    private int statusCode = 200;
    private List<String> messages = new ArrayList<>();

    public void addMessage(String message){
        messages.add(message);
    }

    public static void fromSerializable(Response response,SerializableResponse serResponse) {
        response.setBinaryResponse(serResponse.isBinaryResponse());
        if(serResponse.isBinaryResponse()){
            response.setResponse(serResponse.getResponseBytes());
        }else{
            response.setResponse(serResponse.getResponseText());
        }
        response.setHeaders(serResponse.getHeaders());
        response.setStatusCode(serResponse.getStatusCode());
    }

    public static SerializableResponse toSerializable(Response response) {
        var result = new SerializableResponse();
        result.binaryResponse = response.isBinaryResponse();
        if(response.isBinaryResponse()){
            result.setResponseBytes((byte[])response.getResponse());
        }else {
            result.setResponseText((String)response.getResponse());
        }
        result.setMessages(response.messages);
        result.setHeaders(response.getHeaders());
        result.setStatusCode(response.getStatusCode());
        return result;
    }

    public static Response fromHttpResponse(HttpResponse httpResponse,Response response) throws IOException {

        HttpEntity responseEntity = httpResponse.getEntity();
        if(responseEntity!=null) {
            InputStream in = responseEntity.getContent();

            String contentEncoding = null;
            if (null != responseEntity.getContentEncoding()) {
                contentEncoding = responseEntity.getContentEncoding().getValue().toLowerCase(Locale.ROOT);
            }
            if (responseEntity.getContentType()!=null && responseEntity.getContentType().getValue()!=null && MimeChecker.isBinary(responseEntity.getContentType().getValue(), contentEncoding)) {
                response.responseBytes = IOUtils.toByteArray(in);
                response.binaryResponse = true;
            } else {
                response.responseText = IOUtils.toString(in, StandardCharsets.UTF_8);
            }
        }else{
            response.setBinaryResponse(true);
            response.responseBytes=new byte[0];
        }
        response.statusCode = httpResponse.getStatusLine().getStatusCode();
        for (var header:   httpResponse.getAllHeaders()) {
            if(header.getName().equalsIgnoreCase("transfer-encoding")) continue;
            response.headers.put(header.getName(),header.getValue());
        }
        return response;
    }

    public static Response fromHttpResponse(HttpResponse httpResponse) throws IOException {
        Response response = new Response();
        return fromHttpResponse(httpResponse,response);
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeader(String index,String data){
        this.headers.put(index,data);
    }

    public String removeHeader(String index){
        if(this.headers.containsKey(index)){
            String data = headers.get(index);
            headers.remove(index);
            return data;
        }
        return null;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }

    public boolean isBinaryResponse() {
        return binaryResponse;
    }

    public void setBinaryResponse(boolean binaryResponse) {
        this.binaryResponse = binaryResponse;
    }

    public void setResponse(byte[] responseBytes) {
        this.responseBytes = responseBytes;
    }

    public void setResponse(String responseText) {
        this.responseText = responseText;
    }

    public Object getResponse() {
        if (this.isBinaryResponse()) {
            return this.responseBytes;
        } else {
            return this.responseText;
        }
    }
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String toJsonString() {
        throw new RuntimeException();
    }

    public void addHeader(String key, String value) {
        if(this.headers==null) headers = new HashMap<>();
        headers.put(key,value);
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public String getHeader(String s) {
        return RequestUtils.getFromMap(this.headers,s);
    }
}
