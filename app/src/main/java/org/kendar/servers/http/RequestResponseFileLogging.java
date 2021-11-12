package org.kendar.servers.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.GlobalConfig;
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
    private JsonConfiguration configuration;
    private final FileResourcesUtils fileResourcesUtils;

    public RequestResponseFileLogging(
      FileResourcesUtils fileResourcesUtils,
      LoggerBuilder loggerBuilder,
      JsonConfiguration configuration){

        this.fileResourcesUtils = fileResourcesUtils;
        this.logger = loggerBuilder.build(RequestResponseFileLogging.class);
        this.configuration = configuration;
    }

    @Override
    public String getId() {
        return "org.kendar.servers.http.RequestResponseFileLogging";
    }

    private String logPath;



    @PostConstruct
    public void init(){
        try {
            var config  = configuration.getConfiguration(GlobalConfig.class);
            logPath = fileResourcesUtils.buildPath(config.getLogging().getPath());
            var np = Path.of(logPath);
            if(!Files.isDirectory(np)){
                Files.createDirectory(np);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final ObjectMapper mapper = new ObjectMapper();
    @HttpMethodFilter(phase = HttpFilterType.POST_RENDER,pathAddress ="*",method = "*",id="1001a4b4-277d-11ec-9621-0242ac130002")
    public boolean doLog(Request serReq, Response serRes){
        var config  = configuration.getConfiguration(GlobalConfig.class);
        var logging = config.getLogging();
        if(serReq.isStaticRequest() && !logging.isStatics()) return false;
        if(serReq.isStaticRequest() && !logging.isDynamic()) return false;


        if(!logging.getRequest().isFull()){
            if(serReq.getRequestText()!=null && logging.getRequest().isBasic()) {
                serReq.setRequestText(serReq.getRequestText().substring(0,100));
            }else{
                serReq.setRequestText(null);
            }
            serReq.setRequestBytes(null);
        }
        if(!logging.getResponse().isFull()){
            if(serRes.getResponseText()!=null && logging.getRequest().isBasic()) {
                serRes.setResponseText(serRes.getResponseText().substring(0,100));
            }else{
                serRes.setResponseText(null);
            }
            serRes.setResponseBytes(null);
        }
        var extension = getOptionalExtension(serReq.getPath());
        var filePath = logPath+File.separator+ cleanUp(serReq.getMs()+"_"+serReq.getHost()+"_"+serReq.getPath());
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
