package org.kendar.servers.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.dns.configurations.ExtraDnsServer;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.Header;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.GlobalConfig;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.logging.model.FileLogListItem;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.kendar.xml.model.XmlAttribute;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class FileLogsApi implements FilteringClass {
    final ObjectMapper mapper = new ObjectMapper();
    private final JsonConfiguration configuration;
    private final LoggerBuilder loggerBuilder;
    private final FileResourcesUtils fileResourcesUtils;
    private Path roundtripsPath;

    public FileLogsApi(JsonConfiguration configuration,
                       LoggerBuilder loggerBuilder,
                       FileResourcesUtils fileResourcesUtils){

        this.configuration = configuration;
        this.loggerBuilder = loggerBuilder;
        this.fileResourcesUtils = fileResourcesUtils;
    }
    @PostConstruct
    public void init() throws Exception {
        var config = configuration.getConfiguration(GlobalConfig.class);
        roundtripsPath =  Path.of(fileResourcesUtils.buildPath(config.getLogging().getLogRoundtripsPath()));
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/log/files",
            method = "GET",
            id = "1000aab4-2987-a1ef-5621-0242ac130002")
    @HamDoc(
            description = "List all log files",
            responses = @HamResponse(
                    body = FileLogListItem[].class
            ))
    public void getLogFiles(Request req, Response res) throws JsonProcessingException {
        ArrayList<FileLogListItem> result = getFileLogListItems();
        res.addHeader("Content-type", "application/json");
        res.setResponseText(mapper.writeValueAsString(result.stream().sorted(Comparator.comparing(FileLogListItem::getTimestamp)).collect(Collectors.toList())));

    }

    private ArrayList<FileLogListItem> getFileLogListItems() {
        var result = new ArrayList<FileLogListItem>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS", Locale.ROOT);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(roundtripsPath)) {
            for (Path file: stream) {
                var all =file.getFileName().toString().split("\\.");
                var expl = file.getFileName().toString().split("___",3);
                var newItem = new FileLogListItem();
                newItem.setId(file.getFileName().toString());
                newItem.setHost(expl[1].replaceAll("\\-","."));
                newItem.setPath(expl[2].substring(0,expl[2].length()-all.length-2).replaceAll("\\-","/"));
                newItem.setTimestamp(Long.parseLong(expl[0]));
                var date = new Date(newItem.getTimestamp());
                newItem.setTime(sdf.format(date));
                result.add(newItem);
            }
        } catch (Exception x) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can only be thrown by newDirectoryStream.
            System.err.println(x);
        }
        return result;
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/log/files/{id}",
            method = "GET",
            id = "1000aab489a6s7-a1ef-5621-0242ac130002")
    @HamDoc(
            description = "Retrieve single log file",
            path = @PathParameter(key="id"),
            responses = @HamResponse(
                    body = String.class,
                    description = "Content of the log file",
                    headers = {
                            @Header(key = "X-NEXT", description = "Next file id if present"),
                            @Header(key = "X-PREV", description = "Previous file id if present")
                    }
            ))
    public void getLogFile(Request req, Response res) throws IOException {
        String id = req.getPathParameter("id");
        var data = Files.readString(Path.of(roundtripsPath.toString(),id));
        ArrayList<FileLogListItem> result = getFileLogListItems();
        String past=null;
        String founded = null;
        String next = null;
        for(var resu :result){
            if(founded!=null){
                next = resu.getId();
                break;
            }
            if(founded == null && id.equalsIgnoreCase(resu.getId())){
                founded = id;
            }
            if(founded == null) {
                past = resu.getId();
            }
        }
        if(next!=null){
            res.addHeader("X-NEXT", next);
        }
        if(past!=null){
            res.addHeader("X-PAST", past);
        }
        res.addHeader("Content-type", "application/json");
        res.setResponseText(data);
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }
}
