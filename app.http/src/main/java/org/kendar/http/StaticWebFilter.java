package org.kendar.http;

import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.MimeChecker;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public abstract class StaticWebFilter implements FilteringClass {
    private final FileResourcesUtils fileResourcesUtils;

    public StaticWebFilter(FileResourcesUtils fileResourcesUtils){
        this.fileResourcesUtils = fileResourcesUtils;
    }
    protected abstract String getPath();
    private HashMap<String, Object> resourceFiles = new HashMap<>();



    @PostConstruct
    public void loadAllStuffs() throws IOException, URISyntaxException {
        var realPath = getPath();
        if(isResource(getPath())){
            realPath = realPath.substring(1);
            resourceFiles = fileResourcesUtils.loadResources(this,realPath);
        }
    }


    @HttpMethodFilter(phase = HttpFilterType.STATIC,pathAddress ="*",method = "GET")
    public boolean handle(Request request, Response response) throws IOException {
        var realPath = getPath();
        if(isResource(getPath())){
            realPath = realPath.substring(1);
        }

        if (verifyPath(response, realPath, request.getPath())) return true;
        if (verifyPath(response, realPath, request.getPath()+"/index.htm")) return true;
        if (verifyPath(response, realPath, request.getPath()+"/index.html")) return true;

        response.setStatusCode(404);
        response.setResponseText("Page not found: "+request.getPath());
        return true;
    }

    private boolean verifyPath(Response response, String realPath, String possibleMatch) {
        Path fullPath;
        if(resourceFiles==null || resourceFiles.isEmpty()) {
            fullPath = Path.of(fileResourcesUtils.buildPath(realPath, possibleMatch));
        }else{
            fullPath = Path.of(realPath, possibleMatch);
        }
        if(isFileExisting(fullPath)){
            renderFile(fullPath, response);
            return true;
        }
        return false;
    }

    private boolean isFileExisting(Path fullPath) {
        if(fullPath==null) return false;
        var resourcePath = fullPath.toString().replace('\\','/');
        if(resourceFiles==null || resourceFiles.isEmpty()) {
            return Files.exists(fullPath) && !Files.isDirectory(fullPath);
        }else if(resourceFiles.containsKey(resourcePath)){
            var data = resourceFiles.get(resourcePath);
            if(data==null) return false;
            return ((byte[])data).length>0;
        }else{
            return false;
        }
    }

    private boolean isResource(String path) {
        return path.startsWith("*");
    }

    private void renderFile(Path fullPath, Response response) {
        try {
            String mimeType = null;
            if(resourceFiles==null || resourceFiles.isEmpty()) {
                mimeType = Files.probeContentType(fullPath);
            }
            if (mimeType == null) {
                if(fullPath.toString().endsWith(".js")){
                    mimeType ="text/javascript";
                }else if(fullPath.toString().endsWith(".css")){
                    mimeType ="text/css";
                }else if(fullPath.toString().endsWith(".htm")||fullPath.toString().endsWith(".html")){
                    mimeType ="text/html";
                }else{
                    mimeType = "application/octect-stream";
                }
            }
            response.setBinaryResponse(MimeChecker.isBinary(mimeType,null));
            if(resourceFiles==null || resourceFiles.isEmpty()) {
                if (response.isBinaryResponse()) {
                    response.setResponseBytes(Files.readAllBytes(fullPath));
                } else {
                    response.setResponseText(Files.readString(fullPath));
                }
            }else{
                var resourcePath = fullPath.toString().replace('\\','/');
                if (response.isBinaryResponse()) {
                    response.setResponseBytes((byte[])resourceFiles.get(resourcePath));
                } else {
                    response.setResponseText(new String((byte[])resourceFiles.get(resourcePath)));
                }
            }
            response.addHeader("Content-Type",mimeType);
            response.setStatusCode(200);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
