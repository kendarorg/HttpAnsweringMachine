package org.kendar.replayer.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.replayer.ReplayerConfig;
import org.kendar.replayer.apis.models.RecordingItem;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.FileResourcesUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class ResultsAPI  implements FilteringClass {

    private ObjectMapper mapper = new ObjectMapper();
    private final String replayerData;
    private FileResourcesUtils fileResourcesUtils;
    private final JsonConfiguration configuration;

    public ResultsAPI(JsonConfiguration configuration, FileResourcesUtils fileResourcesUtils){
        this.configuration = configuration;
        this.replayerData = configuration.getConfiguration(ReplayerConfig.class).getPath();
        this.fileResourcesUtils = fileResourcesUtils;
    }

    private Path getRootPath() throws IOException {
        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData));
        if (!Files.isDirectory(rootPath)) {
            Files.createDirectory(rootPath);
        }
        return rootPath;
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/results",
            method = "GET",
            id = "3004daaallress-11ec-9621-0242ac1afe002")
    public void getResults(Request request, Response response) throws IOException {
        var rootPath = getRootPath();
        var result = new ArrayList<RecordingItem>();
        loadFileResults(rootPath, result, "null");
        loadFileResults(rootPath, result, "pacts");
        response.addHeader("content-type","application/json");
        response.setResponseText(mapper.writeValueAsString(result));
    }
    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/results/{id}",
            method = "GET",
            id = "300singss-11ec-9621-0242ac1afe002")
    public void getResult(Request request, Response response) throws IOException {
        var id = request.getPathParameter("id");
        var rootPath = getRootPath();
        var result = new ArrayList<RecordingItem>();
        loadFileResults(rootPath, result, "null");
        loadFileResults(rootPath, result, "pacts");
        var founded = result.stream().filter(a-> a.getFileId().equalsIgnoreCase(id)).findFirst();

        if(founded.isPresent()){
            var item = founded.get();
            var fullFile = Path.of(rootPath + File.separator + item.getTestType() + File.separator+item.getFileId());
            if(Files.exists(fullFile)){
                response.addHeader("content-type","application/json");
                response.setResponseText(Files.readString(fullFile));
                return;
            }
        }
        response.setStatusCode(404);

    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/results/{id}",
            method = "DELETE",
            id = "3004dderess-11ec-9621-0242ac1afe002")
    public void deleteresult(Request request, Response response) throws IOException {
        var id = request.getPathParameter("id");
        var rootPath = getRootPath();
        var result = new ArrayList<RecordingItem>();
        loadFileResults(rootPath, result, "null");
        loadFileResults(rootPath, result, "pacts");

        var founded = result.stream().filter(a-> a.getFileId().equalsIgnoreCase(id)).findFirst();

        if(founded.isPresent()){
            var item = founded.get();
            var fullFile = Path.of(rootPath + File.separator + item.getTestType() + File.separator+item.getFileId());
            if(Files.exists(fullFile)){
                Files.delete(fullFile);
            }
        }
    }

    private void loadFileResults(Path rootPath, ArrayList<RecordingItem> result, String type) {
        var nullDir = Path.of(rootPath + File.separator + type + File.separator);
        if(!Files.exists(nullDir))return;
        File file = new File(nullDir.toString());
        String[] fileList = file.list();
        if(fileList==null){
            return;
        }
        for(String str : fileList) {
            Path path = Paths.get(str);
            Path fileName = path.getFileName();
            var parts = fileName.toString().split("\\.");
            var ra = new RecordingItem();
            ra.setTestType(type);
            ra.setFileId(fileName.toString());
            ra.setName(parts[0]);
            result.add(ra);
        }

    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }
}
