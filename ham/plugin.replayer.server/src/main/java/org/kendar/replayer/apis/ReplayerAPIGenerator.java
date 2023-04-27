package org.kendar.replayer.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.replayer.generator.SelectedGenerator;
import org.kendar.replayer.utils.Md5Tester;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class ReplayerAPIGenerator implements FilteringClass {

    final ObjectMapper mapper = new ObjectMapper();
    private final List<SelectedGenerator> generators;

    public ReplayerAPIGenerator(
            List<SelectedGenerator> generators,
            LoggerBuilder loggerBuilder,
            Md5Tester md5Tester,
            JsonConfiguration configuration) {
        this.generators = generators;
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }


    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/generator/{id}/{type}",
            method = "POST")
    @HamDoc(description = "Generate request response source files with pom (NOT COMPLETE)", tags = {"plugin/replayer"},
            path = @PathParameter(key = "id"),
            responses = @HamResponse(
                    content = "application/zip",
                    body = byte[].class
            )
    )
    public void listAllRecordingSteps(Request req, Response res) throws Exception {
        var id = Integer.parseInt(req.getPathParameter("id"));
        var type = req.getPathParameter("type");
        List<Long> ids = Arrays.stream(mapper.readValue(req.getRequestText(), Long[].class)).collect(Collectors.toList());
        SelectedGenerator generator = null;
        for(var i=0;i<this.generators.size();i++){
            if(this.generators.get(i).getId().equalsIgnoreCase(type)){
                generator = this.generators.get(i);
                break;
            }
        }
        if(generator==null){
            return;
        }

        generator.generate(id,req,res,ids);
    }

}
