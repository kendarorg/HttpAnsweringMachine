package org.kendar.replayer.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.replayer.ReplayerConfig;
import org.kendar.replayer.apis.models.Scripts;
import org.kendar.replayer.storage.CallIndex;
import org.kendar.replayer.storage.DataReorganizer;
import org.kendar.replayer.storage.ReplayerDataset;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

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
            method = "GET")
    @HamDoc(description = "retrieves the scripts associate with a recording line",tags = {"plugin/replayer"},
            path = {@PathParameter(key = "id"),@PathParameter(key = "line")},
            responses = @HamResponse(
                    body = String.class
            )
    )
    public void retrieveScript(Request req, Response res) throws IOException {
        var id = req.getPathParameter("id");
        var line = req.getPathParameter("line");

        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

        var dataset =
                new ReplayerDataset(loggerBuilder, dataReorganizer, md5Tester);
        dataset.load(id, rootPath.toString(),null);
        var datasetContent = dataset.load();
        var prev = -1;
        var next = -1;
        var result = new Scripts();
        var allItems = new ArrayList<>(datasetContent.getDynamicRequests());
        allItems.addAll(new ArrayList<>(datasetContent.getStaticRequests()));
        datasetContent.getIndexes().sort(Comparator.comparingInt(CallIndex::getId));
        for(var i=0;i<datasetContent.getIndexes().size();i++){
            var singleLine = datasetContent.getIndexes().get(i);
            if (singleLine.getId() == Integer.parseInt(line)) {
                var possible = allItems.stream().filter(a->a.getId()==singleLine.getReference()).findFirst();
                if(possible.isEmpty()){
                    continue;
                }
                var ref= possible.get();
                result.setId(Integer.toString(singleLine.getId()));
                result.setHost(ref.getRequest().getHost());
                result.setPath(ref.getRequest().getPath());
                result.setMethod(ref.getRequest().getMethod());
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
        if(datasetContent.getPreScript().containsKey(line)){
            result.setPre(datasetContent.getPreScript().get(line));
        }
        if(datasetContent.getPostScript().containsKey(line)){
            result.setPost(datasetContent.getPostScript().get(line));
        }

        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(result));
    }



    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/recording/{id}/script/{line}",
            method = "DELETE")
    @HamDoc(description = "delete the scripts associate with a recording line",tags = {"plugin/replayer"},
            path = {@PathParameter(key = "id"),@PathParameter(key = "line")}
    )
    public void deleteScript(Request req, Response res) throws IOException {
        var id = req.getPathParameter("id");
        var line = req.getPathParameter("line");

        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

        var dataset =
                new ReplayerDataset(loggerBuilder, dataReorganizer, md5Tester);
        dataset.load(id, rootPath.toString(),null);
        var datasetContent = dataset.load();
        datasetContent.getPostScript().remove(line);
        datasetContent.getPreScript().remove(line);
        dataset.justSave(datasetContent);
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/recording/{id}/script/{line}",
            method = "PUT")
    @HamDoc(description = "modify/insert the scripts associate with a recording line",tags = {"plugin/replayer"},
            path = {@PathParameter(key = "id"),@PathParameter(key = "line")},
            responses = @HamResponse(
                    body = String.class
            )
    )
    public void putScript(Request req, Response res) throws IOException {
        var id = req.getPathParameter("id");
        var lines = Arrays.stream(req.getPathParameter("line").split(","))
                .map(Integer::parseInt).collect(Collectors.toList());


        var data = mapper.readValue(req.getRequestText(),Scripts.class);

        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

        var dataset =
                new ReplayerDataset(loggerBuilder, dataReorganizer, md5Tester);
        dataset.load(id, rootPath.toString(),null);
        var datasetContent = dataset.load();

        for(var line:lines) {
            if (data.getPre() == null || data.getPre().trim().isEmpty()) {
                datasetContent.getPreScript().remove(line + "");
            } else {
                datasetContent.getPreScript().put(line + "", data.getPre().trim());
            }

            if (data.getPost() == null || data.getPost().trim().isEmpty()) {
                datasetContent.getPostScript().remove(line + "");
            } else {
                datasetContent.getPostScript().put(line + "", data.getPost().trim());
            }
        }

        if(lines.size()==1) {
            var line = lines.get(0);
            ReplayerRow foundedRow = null;
            for (var dyr : datasetContent.getDynamicRequests()) {
                if (dyr.getId() == line) {
                    foundedRow = dyr;
                    break;
                }
            }
            for (var dyr : datasetContent.getStaticRequests()) {
                if (dyr.getId() == line) {
                    foundedRow = dyr;
                    break;
                }
            }
            if (foundedRow != null) {
                foundedRow.getRequest().setHost(data.getHost());
                foundedRow.getRequest().setPath(data.getPath());
            }
        }
        dataset.justSave(datasetContent);
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }
}
