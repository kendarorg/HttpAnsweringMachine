package org.kendar.replayer.apis.old;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.ReplayerStatus;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;

@Component
@HttpTypeFilter(hostAddress = "${replayer.address:replayer.local.org}",
        blocking = true)
public class ReplayerApi2 implements FilteringClass {
    private ReplayerStatus replayerStatus;
    private ObjectMapper mapper = new ObjectMapper();

    public ReplayerApi2(ReplayerStatus replayerStatus){
        this.replayerStatus = replayerStatus;
    }


    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress =ReplayerStatus.API,method = "GET")
    public boolean getCurrentStatus(Request request, Response response){
        var map = new HashMap<String,String>();
        map.put("status",replayerStatus.getReplayerState().toString());
        if(replayerStatus.getOperationId()!=null) {
            map.put("operation", replayerStatus.getOperationId());
        }
        response.setStatusCode(200);
        response.addHeader("Content-Type","application/json");
        try {
            response.setResponse(mapper.writeValueAsString(map));
        } catch (JsonProcessingException e) {

        }
        return true;
    }


    @HttpMethodFilter(phase = HttpFilterType.API,pathAddress =ReplayerStatus.RECORDINGS+"/{id}/record",method = "GET")
    public boolean startRecording(Request request, Response response){
        replayerStatus.setOperation(request.getPathParameter("id"), ReplayerState.RECORDING);
        return true;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,pathAddress =ReplayerStatus.RECORDINGS+"/{id}/stop",method = "GET")
    public boolean stopOperation(Request request, Response response){
        replayerStatus.setOperation(ReplayerState.NONE);
        try {
            replayerStatus.save(request.getPathParameter("id"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,pathAddress =ReplayerStatus.RECORDINGS+"/{id}/replay",method = "GET")
    public boolean startReplaying(Request request, Response response){
        replayerStatus.setOperation(request.getPathParameter("id"),ReplayerState.REPLAYING);
        return true;
    }
}
