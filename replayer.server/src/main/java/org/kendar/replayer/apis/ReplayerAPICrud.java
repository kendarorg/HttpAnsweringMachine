package org.kendar.replayer.apis;

import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${replayer.address:replayer.local.org}",
        blocking = true)
public class ReplayerAPICrud implements FilteringClass {

    @Override
    public String getId() {
        return "org.kendar.replayer.apis.ReplayerAPICrud";
    }
    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/recording",
            method = "GET")
    public boolean listAllLocalRecordings(Request req, Response res){

        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/recording",
            method = "POST")
    public boolean uploadRecording(Request req, Response res){
        return false;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/recording/{id}",
            method = "GET")
    public boolean listAllRecordingSteps(Request req, Response res){
        var id = req.getPathParameter("id");
        return false;
    }
}
