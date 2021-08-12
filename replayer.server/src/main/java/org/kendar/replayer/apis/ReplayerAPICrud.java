package org.kendar.replayer.apis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.apis.models.ListAllRecordList;
import org.kendar.replayer.storage.ReplayerDataset;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Logger;

@Component
@HttpTypeFilter(hostAddress = "${replayer.address:replayer.local.org}",
        blocking = true)
public class ReplayerAPICrud implements FilteringClass {
    ObjectMapper mapper = new ObjectMapper();
    @Value("${replayer.data:replayerdata}")
    private String replayerData;

    private FileResourcesUtils fileResourcesUtils;
    private LoggerBuilder loggerBuilder;

    public ReplayerAPICrud(FileResourcesUtils fileResourcesUtils, LoggerBuilder loggerBuilder){

        this.fileResourcesUtils = fileResourcesUtils;

        this.loggerBuilder = loggerBuilder;
    }

    @Override
    public String getId() {
        return "org.kendar.replayer.apis.ReplayerAPICrud";
    }
    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/recording",
            method = "GET")
    public boolean listAllLocalRecordings(Request req, Response res) throws JsonProcessingException {
        var realPath = fileResourcesUtils.buildPath(replayerData);
        var f = new File(realPath);
        var pathnames = f.list();
        var listOfItems = new ArrayList<String>();
        for (var pathname :pathnames) {
            if(pathname.toLowerCase(Locale.ROOT).endsWith(".json")){
                listOfItems.add(pathname.substring(0,pathname.length()-5));
            }

        }
        res.addHeader("Content-type","application/json");
        res.setResponse(mapper.writeValueAsString(listOfItems));
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/recording/{id}",
            method = "GET")
    public boolean listAllRecordingSteps(Request req, Response res) throws IOException {
        var id = req.getPathParameter("id");

        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

        var dataset = new ReplayerDataset(id,rootPath.toString(),null,loggerBuilder,null);
        var datasetContent = dataset.load();
        ListAllRecordList result = new ListAllRecordList(datasetContent,id);
        res.addHeader("Content-type","application/json");
        res.setResponse(mapper.writeValueAsString(result));
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/recording",
            method = "POST")
    public boolean uploadRecording(Request req, Response res){
        return false;
    }
}
