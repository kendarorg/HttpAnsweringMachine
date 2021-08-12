package org.kendar.replayer.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.replayer.storage.ReplayerDataset;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.servers.http.MultipartPart;
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
public class ReplayerAPIContent implements FilteringClass {
    private FileResourcesUtils fileResourcesUtils;
    private LoggerBuilder loggerBuilder;
    ObjectMapper mapper = new ObjectMapper();
    @Value("${replayer.data:replayerdata}")
    private String replayerData;

    public ReplayerAPIContent(FileResourcesUtils fileResourcesUtils, LoggerBuilder loggerBuilder){

        this.fileResourcesUtils = fileResourcesUtils;
        this.loggerBuilder = loggerBuilder;
    }
    @Override
    public String getId() {
        return "org.kendar.replayer.apis.ReplayerAPIContent";
    }
    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/recording/{id}/line/{line}/{requestOrResponse}",
            method = "GET")
    public boolean retrieveContent(Request req, Response res) throws IOException {
        var id = req.getPathParameter("id");
        var line = Integer.parseInt(req.getPathParameter("line"));
        var requestOrResponse = req.getPathParameter("requestOrResponse");

        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

        var dataset = new ReplayerDataset(id,rootPath.toString(),null,loggerBuilder,null);
        var datasetContent = dataset.load();

        for (var singleLine : datasetContent.getStaticRequests()) {
            if (extracted(res, line, requestOrResponse, singleLine)) {
                dataset.save();
                return true;
            }
        }
        for (var singleLine : datasetContent.getDynamicRequests()) {
            if (extracted(res, line, requestOrResponse, singleLine)) {
                dataset.save();
                return true;
            }
        }
        res.setStatusCode(404);
        res.setResponse("Missing id "+id+" with line "+line);
        return false;
    }

    private boolean extracted(Response res, int line, String requestOrResponse, ReplayerRow singleLine) {
        if (singleLine.getId() == line) {
            if ("request".equalsIgnoreCase(requestOrResponse)) {
                res.setHeader("Content-Type", singleLine.getRequest().getHeader("Content-Type"));
                if (singleLine.getRequest().isBinaryRequest()) {
                    res.setResponse(singleLine.getRequest().getRequestBytes());
                } else {
                    res.setResponse(singleLine.getRequest().getRequestText());
                }
            } else if ("response".equalsIgnoreCase(requestOrResponse)) {
                res.setHeader("Content-Type", singleLine.getResponse().getHeader("Content-Type"));
                if (singleLine.getResponse().isBinaryResponse()) {
                    res.setResponse(singleLine.getResponse().getResponseBytes());
                } else {
                    res.setResponse(singleLine.getResponse().getResponseText());
                }
            }
            return true;
        }
        return false;
    }

    private boolean deleted(Response res, int line, String requestOrResponse, ReplayerRow singleLine) {
        if (singleLine.getId() == line) {
            if ("request".equalsIgnoreCase(requestOrResponse)) {
                singleLine.getRequest().setRequestBytes(null);
                singleLine.getRequest().setRequestText(null);
                singleLine.setRequestHash("0");
            } else if ("response".equalsIgnoreCase(requestOrResponse)) {
                singleLine.getResponse().setResponseText(null);
                singleLine.getResponse().setResponseBytes(null);
                singleLine.setResponseHash("0");
            }
            return true;
        }
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/recording/{id}/line/{line}/{requestOrResponse}",
            method = "DELETE")
    public boolean deleteConent(Request req, Response res) throws IOException {
        var id = req.getPathParameter("id");
        var line = Integer.parseInt(req.getPathParameter("line"));
        var requestOrResponse = req.getPathParameter("requestOrResponse");

        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

        var dataset = new ReplayerDataset(id,rootPath.toString(),null,loggerBuilder,null);
        var datasetContent = dataset.load();

        for (var singleLine : datasetContent.getStaticRequests()) {
            if (deleted(res, line, requestOrResponse, singleLine)) {
                dataset.save();
                return true;
            }
        }
        for (var singleLine : datasetContent.getDynamicRequests()) {
            if (deleted(res, line, requestOrResponse, singleLine)) {
                dataset.save();
                return true;
            }
        }
        res.setStatusCode(404);
        res.setResponse("Missing id "+id+" with line "+line);
        return false;
    }



    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/recording/{id}/line/{line}/{requestOrResponse}",
            method = "POST")
    public boolean modifyConent(Request req, Response res) throws IOException {
        var id = req.getPathParameter("id");
        var line = Integer.parseInt(req.getPathParameter("line"));
        var requestOrResponse = req.getPathParameter("requestOrResponse");
        var file = req.getMultipartData().get(0);
        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

        var dataset = new ReplayerDataset(id,rootPath.toString(),null,loggerBuilder,null);
        var datasetContent = dataset.load();

        for (var singleLine : datasetContent.getStaticRequests()) {
            if (updated(res, line, requestOrResponse, singleLine,file)) {
                dataset.save();
                return true;
            }
        }
        for (var singleLine : datasetContent.getDynamicRequests()) {
            if (updated(res, line, requestOrResponse, singleLine,file)) {
                dataset.save();
                return true;
            }
        }
        res.setStatusCode(404);
        res.setResponse("Missing id "+id+" with line "+line);
        return false;
    }
    private boolean updated(Response res, int line, String requestOrResponse, ReplayerRow singleLine, MultipartPart file) {
        this is an error
        if (singleLine.getId() == line) {
            if ("request".equalsIgnoreCase(requestOrResponse)) {
                res.setHeader("Content-Type", singleLine.getRequest().getHeader("Content-Type"));
                if (singleLine.getRequest().isBinaryRequest()) {
                    res.setResponse(singleLine.getRequest().getRequestBytes());
                } else {
                    res.setResponse(singleLine.getRequest().getRequestText());
                }
            } else if ("response".equalsIgnoreCase(requestOrResponse)) {
                res.setHeader("Content-Type", singleLine.getResponse().getHeader("Content-Type"));
                if (singleLine.getResponse().isBinaryResponse()) {
                    res.setResponse(singleLine.getResponse().getResponseBytes());
                } else {
                    res.setResponse(singleLine.getResponse().getResponseText());
                }
            }
            return true;
        }
        return false;
    }
}
