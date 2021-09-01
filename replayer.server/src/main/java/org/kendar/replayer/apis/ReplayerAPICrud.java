package org.kendar.replayer.apis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.ReplayerStatus;
import org.kendar.replayer.apis.models.ListAllRecordList;
import org.kendar.replayer.apis.models.LocalRecording;
import org.kendar.replayer.storage.ReplayerDataset;
import org.kendar.replayer.storage.ReplayerResult;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.RequestUtils;
import org.kendar.servers.http.Response;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;

@Component
@HttpTypeFilter(hostAddress = "${replayer.address:replayer.local.org}",
        blocking = true)
public class ReplayerAPICrud implements FilteringClass {
    private final Logger logger;
    private ReplayerStatus replayerStatus;
    ObjectMapper mapper = new ObjectMapper();
    @Value("${replayer.data:replayerdata}")
    private String replayerData;

    private FileResourcesUtils fileResourcesUtils;
    private LoggerBuilder loggerBuilder;

    public ReplayerAPICrud(FileResourcesUtils fileResourcesUtils, LoggerBuilder loggerBuilder, ReplayerStatus replayerStatus){

        this.fileResourcesUtils = fileResourcesUtils;

        this.loggerBuilder = loggerBuilder;
        this.logger = loggerBuilder.build(ReplayerAPICrud.class);
        this.replayerStatus = replayerStatus;
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
        var listOfItems = new ArrayList<LocalRecording>();
        var currentScript = replayerStatus.getCurrentScript();
        for (var pathname :pathnames) {
            if(pathname.toLowerCase(Locale.ROOT).endsWith(".json")){
                var lr = new LocalRecording();
                var tocheck = pathname.substring(0,pathname.length()-5);
                lr.setId(pathname.substring(0,pathname.length()-5));
                lr.setState(ReplayerState.NONE);
                if(tocheck.toLowerCase(Locale.ROOT).equalsIgnoreCase(currentScript)){
                    lr.setState(replayerStatus.getStatus());
                }
                listOfItems.add(lr);
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
    public boolean uploadRecording(Request req, Response res) throws Exception {

        if(req.getMultipartData()!=null && req.getMultipartData().size()==0){
            var scriptName = (String)req.getRequest();
            var crud = new ReplayerResult();
            crud.setDescription(scriptName);
            crud.setDynamicRequests(new ArrayList<>());
            crud.setStaticRequests(new ArrayList<>());
            crud.setErrors(new ArrayList<>());

            var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData, scriptName+".json"));
            var resultInFile = mapper.writeValueAsString(crud);
            Files.write(rootPath, resultInFile.getBytes(StandardCharsets.UTF_8));
            logger.info("Uploaded replayer binary script " + rootPath);
            res.setStatusCode(200);
        }else {
            for (var mp : req.getMultipartData()) {
                //var contendDisposition = RequestUtils.parseContentDisposition(mp.getHeader("Content-Disposition"));
                if (!mp.isFile()) continue;
                var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData, mp.getFileName()));
                if (!rootPath.toString().toLowerCase(Locale.ROOT).endsWith(".json")) {
                    res.addHeader("Content-type", "application/json");
                    res.setStatusCode(500);
                    return false;
                }
                try {

                    if (mp.getByteData() != null) {
                        Files.write(rootPath, mp.getByteData());
                        logger.info("Uploaded replayer binary script " + rootPath);
                    } else {
                        FileWriter myWriter = new FileWriter(rootPath.toString());
                        myWriter.write(mp.getStringData());
                        myWriter.close();
                        logger.info("Uploaded replayer text script " + rootPath);
                    }

                    res.setStatusCode(200);
                } catch (Exception ex) {
                    res.addHeader("Content-type", "application/json");
                    res.setStatusCode(500);
                    return false;
                }
            }
        }
        return false;
    }
}
