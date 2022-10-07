package org.kendar.servers.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//FIXME Add has content
public class Response {
    private byte[] responseBytes;
    private String responseText;
    private Map<String, String> headers = new HashMap<>();
    private int statusCode = 200;
    private boolean binaryResponse;

    private List<String> messages = new ArrayList<>();


    public byte[] getResponseBytes() {
        return responseBytes;
    }

    public void setResponseBytes(byte[] responseBytes) {
        this.responseBytes = responseBytes;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isBinaryResponse() {
        return binaryResponse;
    }

    public void setBinaryResponse(boolean binaryResponse) {
        this.binaryResponse = binaryResponse;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public String getHeader(String s) {
        if(this.headers==null) this.headers = new HashMap<>();
        return RequestUtils.getFromMap(this.headers,s);
    }

    public void addHeader(String key, String value) {
        RequestUtils.addToMap(headers,key,value);
    }

    public Response copy(){
        var r = new Response();
        r.headers = this.headers.entrySet().stream()
                .collect(Collectors.toMap(t -> (String)t.getKey(), v->(String)v.getValue()));
        r.responseBytes = this.responseBytes!=null?this.responseBytes.clone():this.responseBytes;
        r.responseText = this.responseText!=null?new String(this.responseText):this.responseText;
        r.binaryResponse = this.binaryResponse;
        r.statusCode = this.statusCode;

        return r;
    }

    public void removeHeader(String s) {
        for(var kvp : headers.keySet()){
            if(s.equalsIgnoreCase(kvp)){
                headers.remove(kvp);
                return;
            }
        }
    }
}
