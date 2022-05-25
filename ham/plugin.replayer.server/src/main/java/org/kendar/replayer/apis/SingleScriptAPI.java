package org.kendar.replayer.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.replayer.ReplayerConfig;
import org.kendar.replayer.apis.models.ListAllRecordList;
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
public class SingleScriptAPI {
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
            id = "4001daa6-fff-11ec-9621-0242ac1afe002")
    public void listAllRecordingSteps(Request req, Response res) throws IOException {
        var id = req.getPathParameter("id");

        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));

        var dataset =
                new ReplayerDataset( loggerBuilder, null, md5Tester);
        dataset.load(id, rootPath.toString(),null);
        var datasetContent = dataset.load();
        ListAllRecordList result = new ListAllRecordList(datasetContent, id,true);
        result.getLines().sort(Comparator.comparingInt(ReplayerRow::getId));
        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(result));
    }
}
