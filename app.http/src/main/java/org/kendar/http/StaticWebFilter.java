package org.kendar.http;

import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class StaticWebFilter implements FilteringClass {
    protected abstract String getPath();

    private Path buildPath(String requestPart){
        try {
            if(!requestPart.startsWith("/")){
                requestPart = "/"+requestPart;
            }
            var fp = new URI(getPath());

            if(!fp.isAbsolute()){
                Path currentRelativePath = Paths.get("");
                String s = currentRelativePath.toAbsolutePath().toString();

                return Path.of(s+File.separator+getPath()+requestPart);
            }else {
                return Path.of(getPath()+requestPart);
            }
        } catch (URISyntaxException e) {
            return null;
        }
    }


    @HttpMethodFilter(phase = HttpFilterType.STATIC,pathAddress ="*",method = "GET",blocking = false)
    public boolean handle(Request request, Response response){

        var fullPath = buildPath("/"+request.getPath());
        if(fullPath!=null && Files.exists(fullPath)&& !Files.isDirectory(fullPath)){
            renderFile(fullPath,response);
            return true;
        }
        fullPath = buildPath("/"+request.getPath()+"/index.htm");
        if(fullPath!=null && Files.exists(fullPath) && !Files.isDirectory(fullPath)){
            renderFile(fullPath,response);
            return true;
        }
        fullPath = buildPath("/"+request.getPath()+"/index.html");
        if(fullPath!=null && Files.exists(fullPath)&& !Files.isDirectory(fullPath)){
            renderFile(fullPath,response);
            return true;
        }
        response.setStatusCode(404);
        response.setResponse("Page not found: "+request.getPath());
        return true;
    }

    private void renderFile(Path fullPath, Response response) {
        try {
            File ff=fullPath.toFile();
            String mimeType = Files.probeContentType(fullPath);
            response.setResponse(Files.readAllBytes(fullPath));
            response.getHeaders().put("Content-Type",mimeType);
            response.setStatusCode(200);
            response.setBinaryResponse(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
