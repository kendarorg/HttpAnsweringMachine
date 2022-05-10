package org.kendar.replayer.apis;

import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.replayer.ReplayerStatus;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}",
        blocking = true)
public class ReplayerAPIStatus implements FilteringClass {
    @Override
    public String getId() {
        return this.getClass().getName();
    }
    private final ReplayerStatus replayerStatus;

    public ReplayerAPIStatus(ReplayerStatus replayerStatus){
        this.replayerStatus = replayerStatus;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/status",
            method = "GET",id="300999f-11ec-9621-0242ac1afe002")
    public void recording(Request req, Response res) throws IOException {
        String realStatus;
        String currentScript;
        try{
            realStatus = replayerStatus.getStatus().toString();
            currentScript = replayerStatus.getCurrentScript();
        }catch(Exception ex){
            realStatus = "NONE";
            currentScript = "NONE";
        }
        var status = "{\"status\":\""+realStatus+"\",\"running\":\""+currentScript+"\"}";
        res.setResponseText(status);
        res.addHeader("content-type","application/json");
        res.setStatusCode(200);
    }
}
