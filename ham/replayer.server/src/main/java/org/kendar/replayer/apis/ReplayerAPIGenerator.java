package org.kendar.replayer.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.replayer.ReplayerConfig;
import org.kendar.replayer.generator.SingleRequestGenerator;
import org.kendar.replayer.storage.DataReorganizer;
import org.kendar.replayer.storage.ReplayerResult;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class ReplayerAPIGenerator implements FilteringClass {

    private final FileResourcesUtils fileResourcesUtils;
    private final LoggerBuilder loggerBuilder;
    private final DataReorganizer dataReorganizer;
    final ObjectMapper mapper = new ObjectMapper();
    private final Md5Tester md5Tester;
    private SingleRequestGenerator singleRequestGenerator;
    private final String replayerData;

    public ReplayerAPIGenerator(
            SingleRequestGenerator singleRequestGenerator,
            FileResourcesUtils fileResourcesUtils,
            LoggerBuilder loggerBuilder,
            DataReorganizer dataReorganizer,
            Md5Tester md5Tester,
            JsonConfiguration configuration) {
        this.singleRequestGenerator = singleRequestGenerator;

        this.replayerData = configuration.getConfiguration(ReplayerConfig.class).getPath();

        this.fileResourcesUtils = fileResourcesUtils;
        this.loggerBuilder = loggerBuilder;
        this.dataReorganizer = dataReorganizer;
        this.md5Tester = md5Tester;
    }
    @Override
    public String getId() {
        return this.getClass().getName();
    }


    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/generator/{id}",
            method = "GET",
            id = "4001daa6-277f-11ec-9yy1-0242ac1afe002")
    public void listAllRecordingSteps(Request req, Response res) throws IOException {
        var id = req.getPathParameter("id");
        var pack = req.getQuery("package");

        Map<String, byte[]> result=null;
        var rootPath = Path.of(fileResourcesUtils.buildPath(replayerData, id + ".json"));
        if (Files.exists(rootPath)) {
            var fileContent = FileUtils.readFileToString(rootPath.toFile(),"UTF-8");
            var replayer = mapper.readValue(fileContent, ReplayerResult.class);
            result = singleRequestGenerator.generateRequestResponse(pack,id,replayer);


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);
            for(var item :result.entrySet()) {
                ZipEntry entry = new ZipEntry(item.getKey());
                entry.setSize(item.getValue().length);
                zos.putNextEntry(entry);
                zos.write(item.getValue());
                zos.closeEntry();
            }
            zos.close();
            res.setBinaryResponse(true);
            res.addHeader("Content-type", "application/zip");
            res.addHeader("Content-disposition", "inline;filename=" + id + ".zip");

            res.setResponseBytes(baos.toByteArray());
        }
    }

}
