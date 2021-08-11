package org.kendar.servers.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultipartPart {
    public MultipartPart(List<String> data){
        StringBuffer buffer = new StringBuffer();
        boolean header = true;
        for(String line: data){
            if(line.length()==0){
                header=false;
                continue;
            }
            if(header){
                var headerData = line.split(":",2);
                headers.put(headerData[0],headerData[1]);
            }else{
                if(buffer.length()==0){
                    buffer.append(line);
                }else{
                    buffer.append("\r\n");
                    buffer.append(line);
                }
                this.data = buffer.toString();
            }
        }
    }
    private Map<String,String> headers = new HashMap<>();
    private String data = "";

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
