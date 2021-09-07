package org.kendar.servers.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@HttpTypeFilter(hostAddress = "*")
public class RequestResponseFileLogging  implements FilteringClass {
    private final Logger logger;
    private FileResourcesUtils fileResourcesUtils;

    public RequestResponseFileLogging(FileResourcesUtils fileResourcesUtils, LoggerBuilder loggerBuilder){

        this.fileResourcesUtils = fileResourcesUtils;
        this.logger = loggerBuilder.build(RequestResponseFileLogging.class);
    }

    @Override
    public String getId() {
        return "org.kendar.servers.http.RequestResponseFileLogging";
    }
    @Value("${localhost.logging.request:false}")
    private boolean logRequest = false;
    @Value("${localhost.logging.response:false}")
    private boolean logResponse = false;
    @Value("${localhost.logging.request.full:false}")
    private boolean logRequestFull = false;
    @Value("${localhost.logging.response.full:false}")
    private boolean logResponseFull = false;
    @Value("${localhost.logging.path:recording}")
    private String logPath;
    @Value("${localhost.logging.static:false}")
    private boolean logStaticRequests;
    @Value("${localhost.logging.dynamic:false}")
    private boolean logDynamicRequests;



    @PostConstruct
    public void init(){
        try {
            logPath = fileResourcesUtils.buildPath(logPath);
            var np = Path.of(logPath);
            if(!Files.isDirectory(np)){
                Files.createDirectory(np);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ObjectMapper mapper = new ObjectMapper();
    @HttpMethodFilter(phase = HttpFilterType.POST_RENDER,pathAddress ="*",method = "*")
    public boolean doLog(Request req, Response res){
        if(req.isStaticRequest() && !logStaticRequests) return false;
        if(req.isStaticRequest() && !logDynamicRequests) return false;


        var serReq = Request.toSerializable(req);
        var serRes = Response.toSerializable(res);
        if(!logRequestFull){
            serReq.setRequestText(null);
            serReq.setRequestBytes(null);
        }
        if(!logResponseFull){
            serRes.setResponseText(null);
            serRes.setResponseBytes(null);
        }
        var extension = getOptionalExtension(serReq.getPath());
        var filePath = logPath+File.separator+ cleanUp(req.getMs()+"_"+serReq.getHost()+"_"+serReq.getPath());
        if(extension!=null){
            filePath+="."+extension;
        }
        filePath+=".log";

        try {
            FileWriter myWriter = new FileWriter(filePath);
            myWriter.write(serReq.getMethod() + " " + serReq.getProtocol() + " " +
                    serReq.getHost() + " " + serReq.getPath()+"\n");
            myWriter.write("==========================\n");
            myWriter.write("REQUEST:\n");
            myWriter.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(serReq)+"\n");
            myWriter.write("==========================\n");
            myWriter.write("RESPONSE:\n");
            myWriter.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(serRes)+"\n");
            myWriter.write("==========================\n");
            myWriter.close();
        }catch (Exception ex){

        }

        return false;
    }

    private String getOptionalExtension(String filePath) {
        String extension = null;
        int i = filePath.lastIndexOf('.');
        int p = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));

        if (i > p) {
            extension = filePath.substring(i+1);
        }
        return extension;
    }

    private String cleanUp(String s) {
        String result = "";
        for (var c :s.toCharArray()) {
            if(c=='.')c='-';
            if(c=='\\')c='-';
            if(c=='/')c='-';
            if(c=='`')c='-';
            if(c==':')c='-';
            result+=c;
        }
        return result;
    }
}
