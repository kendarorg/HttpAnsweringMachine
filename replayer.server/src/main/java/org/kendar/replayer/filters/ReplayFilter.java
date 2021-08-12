package org.kendar.replayer.filters;

import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.ReplayerStatus;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "*")
public class ReplayFilter  implements FilteringClass {
    @Value("${replayer.address:replayer.local.org}")
    private String localAddress;

    @Override
    public String getId() {
        return "org.kendar.replayer.filters.RecordFilter";
    }
    private ReplayerStatus replayerStatus;

    public ReplayFilter(ReplayerStatus replayerStatus){
        this.replayerStatus = replayerStatus;
    }
    @HttpMethodFilter(phase = HttpFilterType.PRE_RENDER,pathAddress ="*",method = "*")
    public boolean replay(Request req, Response res){
        if(req.getHost().equalsIgnoreCase(localAddress))return false;
        if(replayerStatus.getStatus()!= ReplayerState.REPLAYING)return false;
        var block = replayerStatus.replay(req,res);
        return block;
    }
}
