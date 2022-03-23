package org.kendar.replayer.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.replayer.ReplayerConfig;
import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.DataReorganizer;
import org.kendar.replayer.storage.ReplayerDataset;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class ReplayerAPIScripts implements FilteringClass {

    private final FileResourcesUtils fileResourcesUtils;
    private final LoggerBuilder loggerBuilder;
    private final DataReorganizer dataReorganizer;
    final ObjectMapper mapper = new ObjectMapper();
    private final Md5Tester md5Tester;
    private final String replayerData;

    public ReplayerAPIScripts(
            FileResourcesUtils fileResourcesUtils,
            LoggerBuilder loggerBuilder,
            DataReorganizer dataReorganizer,
            Md5Tester md5Tester,
            JsonConfiguration configuration) {

        this.replayerData = configuration.getConfiguration(ReplayerConfig.class).getPath();

        this.fileResourcesUtils = fileResourcesUtils;
        this.loggerBuilder = loggerBuilder;
        this.dataReorganizer = dataReorganizer;
        this.md5Tester = md5Tester;
    }
    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/recording/{id}/script/{line}",
            method = "GET",
            id = "5000daa6-277f-11ec-9621-0242ac1afe002script")
    public void retrieveScript(Request req, Response res) throws IOException {
        var id = req.getPathParameter("id");
        var isPre = req.getQuery("pre").equalsIgnoreCase("true");
        var line = req.getPathParameter("line");

        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

        var dataset =
                new ReplayerDataset(loggerBuilder, dataReorganizer, md5Tester);
        dataset.load(id, rootPath.toString(),null);
        var datasetContent = dataset.load();
        var prev = -1;
        var next = -1;
        datasetContent.getIndexes().sort(Comparator.comparingInt(CallIndex::getId));
        for(var i=0;i<datasetContent.getIndexes().size();i++){
            var singleLine = datasetContent.getIndexes().get(i);
            if (singleLine.getId() == Integer.parseInt(id)) {

                if (i > 0) {
                    prev = datasetContent.getIndexes().get(i - 1).getId();
                }
                if (i < (datasetContent.getIndexes().size() - 1)) {
                    next = datasetContent.getIndexes().get(i + 1).getId();
                }
                break;
            }
        }
        res.addHeader("X-NEXT", ""+next);
        res.addHeader("X-PREV", ""+prev);
        String script = "";
        if(isPre){
            if(datasetContent.getPreScript().containsKey(line)){
                script = datasetContent.getPreScript().get(line);
            }
        }else{
            if(datasetContent.getPostScript().containsKey(line)){
                script = datasetContent.getPostScript().get(line);
            }
        }

        res.addHeader("Content-type", "text/plain");
        res.setResponseText(script);
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/recording/{id}/script/{line}",
            method = "PUT",
            id = "5000daa6-277f-11ec-9621-0242ac1afe002scriptput")
    public void putScript(Request req, Response res) throws IOException {
        var id = req.getPathParameter("id");
        var isPre = req.getQuery("pre").equalsIgnoreCase("true");
        var line = req.getPathParameter("line");

        var data = req.getRequestText().trim();
        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

        var dataset =
                new ReplayerDataset(loggerBuilder, dataReorganizer, md5Tester);
        dataset.load(id, rootPath.toString(),null);
        var datasetContent = dataset.load();
        if(isPre){
            datasetContent.getPreScript().put(line,data);
        }else{
            datasetContent.getPostScript().put(line,data);
        }
        dataset.justSave(datasetContent);
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }
}
