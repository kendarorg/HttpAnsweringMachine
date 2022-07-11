package org.kendar.replayer.apis;

import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.ReplayerStatus;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}",
        blocking = true)
public class ReplayerAPIActions implements FilteringClass {

    @Override
    public String getId() {
        return "org.kendar.replayer.apis.ReplayerAPIActions";
    }
    private final ReplayerStatus replayerStatus;

    public ReplayerAPIActions(ReplayerStatus replayerStatus){
        this.replayerStatus = replayerStatus;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/recording/{id}/record/{action}",
            method = "GET",id="3000daa6-277f-11ec-9621-0242ac1afe002")
    @HamDoc(description = "Start/stop/pauses recording" ,tags = {"plugin/replayer"},
            path = {@PathParameter(key = "id"),@PathParameter(key="action", description = "start/pause/stop")}
    )
    public void recording(Request req, Response res) throws IOException {
        var id = req.getPathParameter("id");
        var action = req.getPathParameter("action");

        if(action.equalsIgnoreCase("start") && replayerStatus.getStatus()==ReplayerState.NONE){
            var description = req.getQuery("description");
            replayerStatus.startRecording(id,description);
        }else if(action.equalsIgnoreCase("start") && replayerStatus.getStatus()==ReplayerState.PAUSED_RECORDING){

            replayerStatus.restartRecording();
        }else if(action.equalsIgnoreCase("pause") && replayerStatus.getStatus()==ReplayerState.RECORDING){
            replayerStatus.pauseRecording();
        }else if(action.equalsIgnoreCase("stop") &&
                (replayerStatus.getStatus()==ReplayerState.RECORDING||replayerStatus.getStatus()==ReplayerState.PAUSED_RECORDING)){
            replayerStatus.stopAndSave();
        }
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/recording/{id}/replay/{action}",
            method = "GET",id="3001daa6-277f-11ec-9621-0242ac1afe002")
    @HamDoc(description = "Start/stop/pauses replaying" ,tags = {"plugin/replayer"},
            path = {@PathParameter(key = "id"),@PathParameter(key="action", description = "start/pause/stop")}
    )
    public void replaying(Request req, Response res) throws IOException {
        var id = req.getPathParameter("id");
        var action = req.getPathParameter("action");
        if(action.equalsIgnoreCase("start") && replayerStatus.getStatus()==ReplayerState.NONE){
            replayerStatus.startReplaying(id);
        }else if(action.equalsIgnoreCase("start") && replayerStatus.getStatus()==ReplayerState.PAUSED_REPLAYING){
            replayerStatus.restartReplaying();
        }else if(action.equalsIgnoreCase("pause") && replayerStatus.getStatus()==ReplayerState.REPLAYING){
            replayerStatus.pauseReplaying();
        }else if(action.equalsIgnoreCase("stop") &&
                (replayerStatus.getStatus()==ReplayerState.REPLAYING || replayerStatus.getStatus()==ReplayerState.PAUSED_REPLAYING)){
            replayerStatus.stopReplaying();
        }
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/recording/{id}/pact/{action}",
            method = "GET",id="pacta6-277f-11ec-9621-0242ac1afe002")
    @HamDoc(description = "Start/stop/pauses pact test" ,tags = {"plugin/replayer"},
            path = {@PathParameter(key = "id"),@PathParameter(key="action", description = "start/pause/stop")}
    )
    public void pact(Request req, Response res) throws IOException {
        var id = req.getPathParameter("id");
        var action = req.getPathParameter("action");
        if(action.equalsIgnoreCase("start") && replayerStatus.getStatus()==ReplayerState.NONE){
            String runId = replayerStatus.startPact(id);
        }else if(action.equalsIgnoreCase("stop") &&
                replayerStatus.getStatus()==ReplayerState.PLAYING_PACT){
            replayerStatus.stopPact(id);
        }
    }

    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/recording/{id}/null/{action}",
            method = "GET",id="nullaa6-277f-11ec-9621-0242ac1afe002")
    @HamDoc(description = "Start/stop/pauses null test" ,tags = {"plugin/replayer"},
            path = {@PathParameter(key = "id"),@PathParameter(key="action", description = "start/pause/stop")}
    )
    public void nullReplay(Request req, Response res) throws IOException {
        var id = req.getPathParameter("id");
        var action = req.getPathParameter("action");
        if(action.equalsIgnoreCase("start") && replayerStatus.getStatus()==ReplayerState.NONE){
            String runId = replayerStatus.startNull(id);
        }else if(action.equalsIgnoreCase("stop") &&
                replayerStatus.getStatus()==ReplayerState.PLAYING_NULL_INFRASTRUCTURE){
            replayerStatus.stopNull(id);
        }
    }
}
