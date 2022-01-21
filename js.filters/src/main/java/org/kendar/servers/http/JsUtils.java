package org.kendar.servers.http;

import org.apache.commons.codec.binary.Base64;
import org.kendar.events.EventQueue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class JsUtils {
    private EventQueue queue;
    private String rootPath;

    public JsUtils(EventQueue queue,String rootPath){
        this.queue = queue;
        if(rootPath.endsWith("/")||rootPath.endsWith("\\")){
            rootPath = rootPath.substring(0,rootPath.length()-1);
        }
        this.rootPath = rootPath;
    }

    public void handleEvent(String eventType,String jsonEvent){
        queue.handle(eventType,jsonEvent);
    }
    public String loadFile(String path,boolean binary){
        try {
            if(path.startsWith("/")||path.startsWith("\\")){
                path = path.substring(1);
            }
            path = rootPath+File.separator+path;

            String absolute = new File(path).getCanonicalPath();
            if(absolute.toLowerCase(Locale.ROOT).startsWith(rootPath.toLowerCase(Locale.ROOT))){
                if(!binary){
                    return Files.readString(Path.of(absolute));
                }else{
                    var bytes = Files.readAllBytes(Path.of(absolute));
                    return Base64.encodeBase64String(bytes);
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    public Response httpRequest(Request request){
        return null;
    }
}
