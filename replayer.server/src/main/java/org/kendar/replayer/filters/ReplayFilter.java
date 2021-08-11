package org.kendar.replayer.filters;

import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.ReplayerStatus;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "*")
public class ReplayFilter  implements FilteringClass {
    private ReplayerStatus replayerStatus;

    public ReplayFilter(ReplayerStatus replayerStatus){
        this.replayerStatus = replayerStatus;
    }
    @HttpMethodFilter(phase = HttpFilterType.POST_RENDER,pathAddress ="*",method = "*")
    public boolean replay(Request req, Response res){
        if(replayerStatus.getReplayerState()!= ReplayerState.REPLAYING)return false;
        var block = replayerStatus.replay(req,res);
        return block;
    }
}
