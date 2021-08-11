package org.kendar.replayer.apis;

import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.replayer.ReplayerStatus;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${replayer.address:replayer.local.org}",
        blocking = true)
public class ReplayerCrud2 implements FilteringClass {
    @HttpMethodFilter(phase = HttpFilterType.API,pathAddress = ReplayerStatus.RECORDINGS+"/{id}",method = "DELETE")
    public boolean resetRecording(Request request, Response response){
        return true;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,pathAddress =ReplayerStatus.RECORDINGS+"/{id}",method = "GET")
    public boolean downloadRecording(Request request, Response response){
        return true;
    }

    @HttpMethodFilter(phase = HttpFilterType.API,pathAddress =ReplayerStatus.RECORDINGS,method = "GET")
    public boolean listLocalRecordings(Request request, Response response){
        return true;
    }
}
