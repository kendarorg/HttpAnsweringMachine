package org.kendar.http;

import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.MimeChecker;

import java.nio.file.Files;
import java.nio.file.Path;

public abstract class StaticWebFilter implements FilteringClass {
    private FileResourcesUtils fileResourcesUtils;

    public StaticWebFilter(FileResourcesUtils fileResourcesUtils){
        this.fileResourcesUtils = fileResourcesUtils;
    }
    protected abstract String getPath();



    @HttpMethodFilter(phase = HttpFilterType.STATIC,pathAddress ="*",method = "GET",blocking = false)
    public boolean handle(Request request, Response response){


        var fullPath = Path.of(fileResourcesUtils.buildPath(getPath(),request.getPath()));
        System.out.println("HANDLING "+fullPath);
        if(fullPath!=null && Files.exists(fullPath)&& !Files.isDirectory(fullPath)){
            renderFile(fullPath,response);
            return true;
        }
        fullPath = Path.of(fileResourcesUtils.buildPath(getPath(),request.getPath()+"/index.htm"));
        if(fullPath!=null && Files.exists(fullPath) && !Files.isDirectory(fullPath)){
            renderFile(fullPath,response);
            return true;
        }
        fullPath = Path.of(fileResourcesUtils.buildPath(getPath(),request.getPath()+"/index.html"));
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
            String mimeType = Files.probeContentType(fullPath);
            if (mimeType == null) {
                if(fullPath.toString().endsWith(".js")){
                    mimeType ="text/javascript";
                }else if(fullPath.toString().endsWith(".css")){
                    mimeType ="text/css";
                }else if(fullPath.toString().endsWith(".htm")||fullPath.toString().endsWith(".html")){
                    mimeType ="text/html";
                }
            }
            response.setBinaryResponse(MimeChecker.isBinary(mimeType,null));
            if(response.isBinaryResponse()) {
                response.setResponse(Files.readAllBytes(fullPath));
            }else{
                response.setResponse(Files.readString(fullPath));
            }
            response.getHeaders().put("Content-Type",mimeType);
            response.setStatusCode(200);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
