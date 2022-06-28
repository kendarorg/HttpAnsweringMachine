package org.kendar.replayer.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.replayer.ReplayerConfig;
import org.kendar.replayer.apis.models.ListAllRecordList;
import org.kendar.replayer.apis.models.SingleScript;
import org.kendar.replayer.apis.models.SingleScriptLine;
import org.kendar.replayer.storage.DataReorganizer;
import org.kendar.replayer.storage.ReplayerDataset;
import org.kendar.replayer.storage.ReplayerResult;
import org.kendar.replayer.storage.ReplayerRow;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.RequestUtils;
import org.kendar.servers.http.Response;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class SingleScriptAPI implements FilteringClass {
    final ObjectMapper mapper = new ObjectMapper();

    private final String replayerData;
    private final FileResourcesUtils fileResourcesUtils;
    private final LoggerBuilder loggerBuilder;
    private final DataReorganizer dataReorganizer;
    private final Md5Tester md5Tester;

    public SingleScriptAPI(
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
            pathAddress = "/api/plugins/replayer/v2/recording/{id}",
            method = "GET",
            id = "4001daa6-fff-11ec-9tar1-0242ac1afe002")
    @HamDoc(todo = true,tags = {"plugin/replayer"},
            path = @PathParameter(key = "id")
    )
    public void listAllRecordingSteps(Request req, Response res) throws IOException {
        var id = req.getPathParameter("id");

        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

        var dataset =
                new ReplayerDataset( loggerBuilder, null, md5Tester);
        dataset.load(id, rootPath.toString(),null);
        var datasetContent = dataset.load();

        var result = convertDataset(datasetContent, id,true);


        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(result));
    }

    private SingleScript convertDataset(ReplayerResult datasetContent, String id, boolean b) {
        var result = new SingleScript();

        var lines = new ArrayList<ReplayerRow>();
        for (var staticLine :
                datasetContent.getStaticRequests()) {
            staticLine.getRequest().setRequestText(null);
            staticLine.getRequest().setRequestBytes(null);
            staticLine.getResponse().setResponseBytes(null);
            staticLine.getResponse().setResponseText(null);
            lines.add((staticLine));
        }
        for (var dynamicLine :
                datasetContent.getDynamicRequests()) {
            dynamicLine.getRequest().setRequestText(null);
            dynamicLine.getRequest().setRequestBytes(null);
            dynamicLine.getResponse().setResponseBytes(null);
            dynamicLine.getResponse().setResponseText(null);
            lines.add((dynamicLine));
        }

        //variables = datasetContent.getVariables();
        //preScript = datasetContent.getPreScript();
        //postScript= datasetContent.getPostScript();

        for(var index: datasetContent.getIndexes()){
            var referencedRowOption = lines.stream()
                    .filter(replayerRow -> replayerRow.getId()==index.getReference())
                    .findFirst();
            if(referencedRowOption.isEmpty())continue;
            var line = referencedRowOption.get();
            var newLine = new SingleScriptLine();
            newLine.setId(index.getId());
            newLine.setRequestMethod(line.getRequest().getMethod());
            newLine.setRequestPath(line.getRequest().getPath());
            newLine.setRequestHost(line.getRequest().getHost());
            newLine.setReference(index.getReference());
            newLine.setPactTest(index.isPactTest());
            newLine.setStimulatorTest(index.isStimulatorTest());
            newLine.setStimulatedTest(line.isStimulatedTest());
            newLine.setQueryCalc(RequestUtils.buildFullQuery(line.getRequest()));
            newLine.setPreScript(datasetContent.getPreScript().containsKey(String.valueOf(line.getId())));
            newLine.setScript(datasetContent.getPostScript().containsKey(String.valueOf(line.getId())));
            newLine.setRequestHashCalc(isHashPresent(line.getRequestHash()));
            newLine.setResponseHashCalc(isHashPresent(line.getResponseHash()));
            newLine.setResponseStatusCode(line.getResponse().getStatusCode());
            result.getLines().add(newLine);
        }
        result.setId(id);
        result.setDescription(datasetContent.getDescription());
        return result;
    }

    private boolean isHashPresent(String hash) {
        return hash!=null && !hash.isEmpty() && !hash.equalsIgnoreCase("0");
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }
}
