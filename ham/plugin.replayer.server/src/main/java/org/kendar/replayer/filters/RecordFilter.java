package org.kendar.replayer.filters;

import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.replayer.ReplayerState;
import org.kendar.replayer.engine.ReplayerEngine;
import org.kendar.replayer.engine.ReplayerStatus;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.GlobalConfig;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@HttpTypeFilter(hostAddress = "*")
public class RecordFilter  implements FilteringClass {
    private final String localAddress;
    @Override
    public String getId() {
        return "org.kendar.replayer.filters.RecordFilter";
    }
    private final Logger logger;
    private List<ReplayerEngine> replayerEngines;
    private final ReplayerStatus replayerStatus;

    public RecordFilter(ReplayerStatus replayerStatus, LoggerBuilder loggerBuilder, JsonConfiguration configuration,
                        List<ReplayerEngine> replayerEngines){
        this.replayerStatus = replayerStatus;
        this.logger = loggerBuilder.build(RecordFilter.class);
        this.replayerEngines = replayerEngines;
        var config = configuration.getConfiguration(GlobalConfig.class);
        localAddress = config.getLocalAddress();
    }

    @HttpMethodFilter(phase = HttpFilterType.POST_RENDER,pathAddress ="*",method = "*",id="9000daa6-277f-11ec-9621-0242ac1afe002")
    public boolean record(Request reqArrived, Response res){
        var req = reqArrived.retrieveOriginal();
        if(req.getPath().contains("api/dns/lookup"))return false;
        var validAddress = false;
        for(var i=0;i<replayerEngines.size();i++){
            validAddress = replayerEngines.get(i).isValidPath(req)||validAddress;
        }
        if(!validAddress)return false;
        if(replayerStatus.getStatus()!= ReplayerState.RECORDING)return false;
        try {
            logger.info("Recording: "+
                    req.getProtocol()+"://"+
                    req.getHost()+
                    req.getPath());
            replayerStatus.addRequest(req,res);
        } catch (Exception e) {
            logger.error("Error recording data",e);
        }
        return false;
    }
}
