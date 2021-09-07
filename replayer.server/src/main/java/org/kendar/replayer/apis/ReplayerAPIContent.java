package org.kendar.replayer.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.replayer.storage.DataReorganizer;
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
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
@HttpTypeFilter(hostAddress = "${replayer.address:replayer.local.org}",
        blocking = true)
public class ReplayerAPIContent implements FilteringClass {
    private FileResourcesUtils fileResourcesUtils;
    private LoggerBuilder loggerBuilder;
    private DataReorganizer dataReorganizer;
    ObjectMapper mapper = new ObjectMapper();
    @Value("${replayer.data:replayerdata}")
    private String replayerData;

    public ReplayerAPIContent(FileResourcesUtils fileResourcesUtils, LoggerBuilder loggerBuilder, DataReorganizer dataReorganizer){

        this.fileResourcesUtils = fileResourcesUtils;
        this.loggerBuilder = loggerBuilder;
        this.dataReorganizer = dataReorganizer;
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

        var dataset = new ReplayerDataset(id,rootPath.toString(),null,loggerBuilder,dataReorganizer);
        var datasetContent = dataset.load();

        for (var singleLine : datasetContent.getStaticRequests()) {
            if (sendBackContent(res, line, requestOrResponse, singleLine)) {
                return true;
            }
        }
        for (var singleLine : datasetContent.getDynamicRequests()) {
            if (sendBackContent(res, line, requestOrResponse, singleLine)) {
                return true;
            }
        }
        res.setStatusCode(404);
        res.setResponse("Missing id "+id+" with line "+line);
        return false;
    }

    private boolean sendBackContent(Response res, int line, String requestOrResponse, ReplayerRow singleLine) {
        if (singleLine.getId() == line) {
            if ("request".equalsIgnoreCase(requestOrResponse)) {
                res.setHeader("Content-Type", singleLine.getRequest().getHeader("Content-Type"));
                res.setBinaryResponse(singleLine.getRequest().isBinaryRequest());
                if (singleLine.getRequest().isBinaryRequest()) {
                    res.setResponse(singleLine.getRequest().getRequestBytes());
                } else {
                    res.setResponse(singleLine.getRequest().getRequestText());
                }
            } else if ("response".equalsIgnoreCase(requestOrResponse)) {
                res.setHeader("Content-Type", singleLine.getResponse().getHeader("Content-Type"));
                res.setBinaryResponse(singleLine.getResponse().isBinaryResponse());
                if (singleLine.getResponse().isBinaryResponse()) {
                    res.setResponse(singleLine.getResponse().getResponseBytes());
                } else {
                    res.setResponse(singleLine.getResponse().getResponseText());
                }
            }
            if(res.isBinaryResponse() && res.getHeader("Content-Type")==null){
                res.setHeader("Content-Type", "application/octect-stream");
            }else{
                res.setHeader("Content-Type", "text/plain");
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

        var dataset = new ReplayerDataset(id,rootPath.toString(),null,loggerBuilder,dataReorganizer);
        var datasetContent = dataset.load();

        for (var singleLine : datasetContent.getStaticRequests()) {
            if (deleted(res, line, requestOrResponse, singleLine)) {
                dataset.saveMods();
                return true;
            }
        }
        for (var singleLine : datasetContent.getDynamicRequests()) {
            if (deleted(res, line, requestOrResponse, singleLine)) {
                dataset.saveMods();
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
    public boolean modifyConent(Request req, Response res) throws IOException, NoSuchAlgorithmException {
        var id = req.getPathParameter("id");
        var line = Integer.parseInt(req.getPathParameter("line"));
        var requestOrResponse = req.getPathParameter("requestOrResponse");
        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));
        MultipartPart file = null;
        String data = null;
        if(req.getMultipartData()!=null && req.getMultipartData().size()>0) {
            file = req.getMultipartData().get(0);
        }else{
            data = (String)req.getRequest();
        }

        var dataset = new ReplayerDataset(id,rootPath.toString(),null,loggerBuilder,dataReorganizer);
        var datasetContent = dataset.load();

        for (var singleLine : datasetContent.getStaticRequests()) {
            if (updated(res, line, requestOrResponse, singleLine,file,data)) {
                dataset.saveMods();
                return true;
            }
        }
        for (var singleLine : datasetContent.getDynamicRequests()) {
            if (updated(res, line, requestOrResponse, singleLine,file,data)) {
                dataset.saveMods();
                return true;
            }
        }
        res.setStatusCode(404);
        res.setResponse("Missing id "+id+" with line "+line);
        return false;
    }

    private String calculateMd5(Object data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        if(data==null){
            return "0";
        }
        if(data instanceof String){
            if(((String)data).length()==0) return "0";
            md.update(((String)data).getBytes(StandardCharsets.UTF_8));
        }else{
            if(((byte[])data).length==0) return "0";
            md.update((byte[])data);
        }
        byte[] digest = md.digest();
        BigInteger bigInt = new BigInteger(1,digest);
        return bigInt.toString(16);
    }


    private boolean updated(Response res, int line, String requestOrResponse, ReplayerRow singleLine, MultipartPart file,String data) throws NoSuchAlgorithmException {
        if (singleLine.getId() == line) {
            if ("request".equalsIgnoreCase(requestOrResponse)) {
                if(singleLine.getRequest().isBinaryRequest()){
                    if (file!=null && file.getByteData()!=null) {
                        singleLine.getRequest().setRequestBytes(file.getByteData());
                        singleLine.setRequestHash(calculateMd5(file.getByteData()));
                    }
                }else{
                    if (file!=null && file.getByteData()!=null) {
                        singleLine.getRequest().setRequestText(new String(file.getByteData(), StandardCharsets.UTF_8));
                        singleLine.setRequestHash(calculateMd5(file.getByteData()));
                    }else{
                        singleLine.getRequest().setRequestText(data);
                        singleLine.setRequestHash(calculateMd5(data.getBytes(StandardCharsets.UTF_8)));
                    }
                }
            } else if ("response".equalsIgnoreCase(requestOrResponse)) {
                if(singleLine.getResponse().isBinaryResponse()){
                    if (file!=null && file.getByteData()!=null) {
                        singleLine.getResponse().setResponseBytes(file.getByteData());
                        singleLine.setResponseHash(calculateMd5(file.getByteData()));
                    }
                }else{
                    if (file!=null && file.getByteData()!=null) {
                        singleLine.getResponse().setResponseText(new String(file.getByteData(), StandardCharsets.UTF_8));
                        singleLine.setResponseHash(calculateMd5(file.getByteData()));
                    }else{
                        singleLine.getResponse().setResponseText(data);
                        singleLine.setResponseHash(calculateMd5(data.getBytes(StandardCharsets.UTF_8)));
                    }
                }
            }
            return true;
        }
        return false;
    }
}
