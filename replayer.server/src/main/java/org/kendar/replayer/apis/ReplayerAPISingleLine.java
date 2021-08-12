package org.kendar.replayer.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.replayer.apis.models.ListAllRecordLine;
import org.kendar.replayer.apis.models.ListAllRecordList;
import org.kendar.replayer.storage.ReplayerDataset;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
@HttpTypeFilter(hostAddress = "${replayer.address:replayer.local.org}",
        blocking = true)
public class ReplayerAPISingleLine implements FilteringClass {

    private FileResourcesUtils fileResourcesUtils;
    private LoggerBuilder loggerBuilder;
    ObjectMapper mapper = new ObjectMapper();
    @Value("${replayer.data:replayerdata}")
    private String replayerData;

    public ReplayerAPISingleLine(FileResourcesUtils fileResourcesUtils, LoggerBuilder loggerBuilder){

        this.fileResourcesUtils = fileResourcesUtils;
        this.loggerBuilder = loggerBuilder;
    }
    @Override
    public String getId() {
        return "org.kendar.replayer.apis.ReplayerAPISingleLine";
    }
    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/recording/{id}/line/{line}",
            method = "GET")
    public boolean retrieveSingleLineData(Request req, Response res) throws IOException {
        var id = req.getPathParameter("id");
        var line = Integer.parseInt(req.getPathParameter("line"));

        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

        var dataset = new ReplayerDataset(id,rootPath.toString(),null,loggerBuilder,null);
        var datasetContent = dataset.load();
        ListAllRecordList result = new ListAllRecordList(datasetContent,id);
        for (var singleLine :
                result.getLines()) {
            if(singleLine.getId()==line){
                res.addHeader("Content-type","application/json");
                res.setResponse(mapper.writeValueAsString(singleLine));
                return true;
            }
        }
        res.setStatusCode(404);
        res.setResponse("Missing id "+id+" with line "+line);

        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/recording/{id}/line/{line}",
            method = "PUT")
    public boolean modifySingleLineData(Request req, Response res) throws IOException {
        var id = req.getPathParameter("id");
        var line = Integer.parseInt(req.getPathParameter("line"));

        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

        var dataset = new ReplayerDataset(id,rootPath.toString(),null,loggerBuilder,null);
        var datasetContent = dataset.load();
        ListAllRecordList result = new ListAllRecordList(datasetContent,id);
        for (var singleLine :
                result.getLines()) {
            if(singleLine.getId()==line){
                var passedLine = mapper.readValue((String)req.getRequest(), ListAllRecordLine.class);
                ReplayerRow row = convertToRow(passedLine);
                dataset.update(row);
                dataset.save();
                return true;
            }
        }
        res.setStatusCode(404);
        res.setResponse("Missing id "+id+" with line "+line);
        return false;
    }


    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/recording/{id}/line",
            method = "POST")
    public boolean addLineData(Request req, Response res) throws IOException {
        var id = req.getPathParameter("id");
        var line = Integer.parseInt(req.getPathParameter("line"));

        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

        var dataset = new ReplayerDataset(id,rootPath.toString(),null,loggerBuilder,null);
        dataset.load();
        var passedLine = mapper.readValue((String)req.getRequest(), ListAllRecordLine.class);
        ReplayerRow row = convertToRow(passedLine);
        dataset.add(row);
        dataset.save();
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/recording/{id}/line/{line}",
            method = "DELETE")
    public boolean deleteSingleLineData(Request req, Response res) throws IOException {
        var id = req.getPathParameter("id");
        var line = Integer.parseInt(req.getPathParameter("line"));

        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

        var dataset = new ReplayerDataset(id,rootPath.toString(),null,loggerBuilder,null);
        dataset.delete(line);
        dataset.save();
        res.setStatusCode(404);
        res.setResponse("Missing id "+id+" with line "+line);
        return false;
    }

    private ReplayerRow convertToRow(ListAllRecordLine passedLine) {
        return null;
    }
}
