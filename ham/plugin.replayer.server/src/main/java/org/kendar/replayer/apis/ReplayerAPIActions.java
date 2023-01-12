package org.kendar.replayer.apis;

import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.http.annotations.multi.QueryString;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.engine.ReplayerStatus;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

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
            method = "GET")
    @HamDoc(description = "Start/stop/pauses recording" ,tags = {"plugin/replayer"},
            path = {
                    @PathParameter(key = "id"),
                    @PathParameter(key="action", description = "start/pause/stop")
            },
            query = {
                    @QueryString(key = "recordDbCalls",type = "boolean",description = "True when recording db calls"),
                    @QueryString(key = "recordVoidDbCalls",type = "boolean",description = "True when recording even " +
                            "db calls that do not return values")
            }
    )
    public void recording(Request req, Response res) throws Exception {
        var id = Long.valueOf(req.getPathParameter("id"));
        var action = req.getPathParameter("action");

        if(action.equalsIgnoreCase("start") && replayerStatus.getStatus()==ReplayerState.NONE){
            var description = req.getQuery("description");
            var recordVoidDbCalls=Boolean.parseBoolean(req.getQuery("recordVoidDbCalls"));
            var recordDbCalls=Boolean.parseBoolean(req.getQuery("recordDbCalls"));
            replayerStatus.startRecording(id,description,recordDbCalls,recordVoidDbCalls);
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
            method = "GET")
    @HamDoc(description = "Start/stop/pauses replaying" ,tags = {"plugin/replayer"},
            path = {@PathParameter(key = "id"),@PathParameter(key="action", description = "start/pause/stop")}
    )
    public void handleReplay(Request req, Response res) throws Exception {
        var id = Long.valueOf(req.getPathParameter("id"));
        var action = req.getPathParameter("action");
        if(action.equalsIgnoreCase("start") && replayerStatus.getStatus()==ReplayerState.NONE){
            Long runId = replayerStatus.startReplaying(id);
        }else if(action.equalsIgnoreCase("start") && replayerStatus.getStatus()==ReplayerState.PAUSED_REPLAYING){
            replayerStatus.restartReplaying(id);
        }else if(action.equalsIgnoreCase("pause") && replayerStatus.getStatus()==ReplayerState.REPLAYING){
            replayerStatus.pauseReplaying(id);
        }else if(action.equalsIgnoreCase("stop") &&
                replayerStatus.getStatus()==ReplayerState.REPLAYING){
            replayerStatus.stopReplaying(id);
        }
    }
    @HttpMethodFilter(phase = HttpFilterType.API,
            pathAddress = "/api/plugins/replayer/recording/{id}/auto/{action}",
            method = "GET")
    @HamDoc(description = "Start/stop/pauses stimulator tests. If not replaying starts it" ,tags = {"plugin/replayer"},
            path = {@PathParameter(key = "id"),@PathParameter(key="action", description = "start/pause/stop")}
    )
    public void handleAutoTest(Request req, Response res) throws Exception {
        var id = Long.valueOf(req.getPathParameter("id"));
        var action = req.getPathParameter("action");
        if(action.equalsIgnoreCase("start") && replayerStatus.getStatus()==ReplayerState.NONE){
            Long runId = replayerStatus.startReplaying(id);
        }else if(action.equalsIgnoreCase("start") && replayerStatus.getStatus()==ReplayerState.PAUSED_REPLAYING){
            replayerStatus.restartReplaying(id);
        }else if(action.equalsIgnoreCase("pause") && replayerStatus.getStatus()==ReplayerState.REPLAYING){
            replayerStatus.pauseReplaying(id);
        }else if(action.equalsIgnoreCase("start") && replayerStatus.getStatus()==ReplayerState.REPLAYING){
            replayerStatus.startStimulator(id);
        }else if(action.equalsIgnoreCase("stop") &&
                replayerStatus.getStatus()==ReplayerState.REPLAYING){
            replayerStatus.stopReplaying(id);
        }
    }
}
