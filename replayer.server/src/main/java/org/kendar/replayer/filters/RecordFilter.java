package org.kendar.replayer.filters;

import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.ReplayerStatus;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.GlobalConfig;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "*")
public class RecordFilter  implements FilteringClass {
    private String localAddress;
    @Override
    public String getId() {
        return "org.kendar.replayer.filters.RecordFilter";
    }
    private final Logger logger;
    private final ReplayerStatus replayerStatus;

    public RecordFilter(ReplayerStatus replayerStatus, LoggerBuilder loggerBuilder, JsonConfiguration configuration){
        this.replayerStatus = replayerStatus;
        this.logger = loggerBuilder.build(RecordFilter.class);
        var config = configuration.getConfiguration(GlobalConfig.class);
        localAddress = config.getLocalAddress();
    }

    @HttpMethodFilter(phase = HttpFilterType.POST_RENDER,pathAddress ="*",method = "*",id="9000daa6-277f-11ec-9621-0242ac1afe002")
    public boolean record(Request req, Response res){
        if(req.getHost().equalsIgnoreCase(localAddress))return false;
        if(replayerStatus.getStatus()!= ReplayerState.RECORDING)return false;
        try {
            replayerStatus.addRequest(req,res);
        } catch (Exception e) {
            logger.error("Error recording data",e);
        }
        return false;
    }
}
