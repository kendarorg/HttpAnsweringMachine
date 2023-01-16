package org.kendar.replayer.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.replayer.ReplayerConfig;
import org.kendar.replayer.generator.SingleRequestGenerator;
import org.kendar.replayer.engine.ReplayerResult;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
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
public class TODOReplayerAPIGenerator implements FilteringClass {

    private final FileResourcesUtils fileResourcesUtils;
    final ObjectMapper mapper = new ObjectMapper();
    private final SingleRequestGenerator singleRequestGenerator;

    public TODOReplayerAPIGenerator(
            SingleRequestGenerator singleRequestGenerator,
            FileResourcesUtils fileResourcesUtils,
            LoggerBuilder loggerBuilder,
            Md5Tester md5Tester,
            JsonConfiguration configuration) {
        this.singleRequestGenerator = singleRequestGenerator;


        this.fileResourcesUtils = fileResourcesUtils;
    }
    @Override
    public String getId() {
        return this.getClass().getName();
    }


    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/generator/{id}",
            method = "GET")
    @HamDoc(description = "Generate request response source files with pom (NOT COMPLETE)",tags = {"plugin/replayer"},
            path = @PathParameter(key = "id"),
            responses = @HamResponse(
                    content = "application/zip",
                    body = byte[].class
            )
    )
    public void listAllRecordingSteps(Request req, Response res) throws IOException {
        var id = req.getPathParameter("id");
        var pack = req.getQuery("package");

        Map<String, byte[]> result;
        var rootPath = Path.of(fileResourcesUtils.buildPath("replayerData", id + ".json"));
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
            res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.ZIP);
            res.addHeader("Content-disposition", "inline;filename=" + id + ".zip");

            res.setResponseBytes(baos.toByteArray());
        }
    }

}
