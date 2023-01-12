package org.kendar.replayer.apis;

import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.Example;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.replayer.engine.ReplayerStatus;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
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
    @HamDoc(description = "Retrieve the current running script",
            responses = @HamResponse(
                    body=String.class,
                    examples = @Example(
                            example = "{\"status\":\"RECORDING\",\"running\":\"TestScript\"}"
                    )
            ),
            tags = {"plugin/replayer"})
    public void recording(Request req, Response res) throws IOException {
        String realStatus;
        Long currentScript;
        try{
            realStatus = replayerStatus.getStatus().toString();
            currentScript = replayerStatus.getCurrentScript();
        }catch(Exception ex){
            realStatus = "NONE";
            currentScript = null;
        }
        var status = "{\"status\":\""+realStatus+"\",\"running\":\""+currentScript+"\"}";
        res.setResponseText(status);
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setStatusCode(200);
    }
}
