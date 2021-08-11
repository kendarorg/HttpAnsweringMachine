package org.kendar.servers.http;

import com.sun.net.httpserver.Headers;
import org.apache.http.entity.ContentType;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestUtils {
    public static boolean isMethodWithBody(Request result) {
        return result.getMethod().equalsIgnoreCase("POST") ||
                result.getMethod().equalsIgnoreCase("PUT") ||
                result.getMethod().equalsIgnoreCase("PATCH");
    }
    public static String getFromMap(Map<String, String> map, String index) {

        if(map.containsKey(index)){
            return map.get(index);
        }
        for (var entry: map.entrySet()) {
            if(entry.getKey().equalsIgnoreCase(index)){
                return entry.getValue();
            }
        }
        return null;
    }

    public static String removeFromMap(Map<String, String> map, String index) {
        if(map.containsKey(index)){
            String data = map.get(index);
            map.remove(index);
            return data;
        }
        return null;
    }

    public static Map<String, String> queryToMap(String qs) {
        Map<String, String> result = new HashMap<>();
        if (qs == null)
            return result;

        int last = 0, next, l = qs.length();
        while (last < l) {
            next = qs.indexOf('&', last);
            if (next == -1)
                next = l;

            if (next > last) {
                int eqPos = qs.indexOf('=', last);
                try {
                    if (eqPos < 0 || eqPos > next)
                        result.put(URLDecoder.decode(qs.substring(last, next), "utf-8"), "");
                    else
                        result.put(URLDecoder.decode(qs.substring(last, eqPos), "utf-8"), URLDecoder.decode(qs.substring(eqPos + 1, next), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e); // will never happen, utf-8 support is mandatory for java
                }
            }
            last = next + 1;
        }
        return result;
    }

    public static Map<String, String> headersToMap(Headers requestHeaders) {
        var result = new HashMap<String,String>();
        for(var entry:requestHeaders.entrySet()){
            if(entry.getValue()==null || entry.getValue().size()==0){
                result.put(entry.getKey(),"");
            }else{
                result.put(entry.getKey(),entry.getValue().get(0));
            }
        }

        return result;
    }

    public static Map<String,String> parseContentDisposition(String value){
        var result = new HashMap<String,String>();
        var cd = ContentDisposition.parse(value);

        result.put("charset",cd.getCharset());
        result.put("filename",cd.getFilename()==null?"file":cd.getFilename());
        result.put("name",cd.getName()==null?"file":cd.getName());
        result.put("type",cd.getType()==null?"application/octect-stream":cd.getType());
        return result;
    }

    public static String sanitizePath(Request result) {
        return result.getHost()+result.getPath();
    }

    public static List<MultipartPart> buildMultipart(String[] splittedText, String boundary) {
        var blocks = new ArrayList<List<String>>();
        for (String line: splittedText) {
            if(line.contains(boundary)){
                blocks.add(new ArrayList<>());
            }else {
                blocks.get(blocks.size() - 1).add(line);
            }
        }
        List<MultipartPart> result = new ArrayList<>();
        for(List<String> block:blocks){
            if(block.size()==0)continue;
            result.add(new MultipartPart(block));
        }
        return result;
    }
}
