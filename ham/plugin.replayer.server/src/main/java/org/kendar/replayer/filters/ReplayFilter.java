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
@HttpTypeFilter(hostAddress = "*", priority = 200)
public class ReplayFilter implements FilteringClass {
    private final ReplayerStatus replayerStatus;
    private final String localAddress;
    private final Logger logger;
    private final List<ReplayerEngine> replayerEngines;

    public ReplayFilter(ReplayerStatus replayerStatus, JsonConfiguration configuration, LoggerBuilder loggerBuilder,
                        List<ReplayerEngine> replayerEngines) {
        this.replayerStatus = replayerStatus;
        this.localAddress = configuration.getConfiguration(GlobalConfig.class).getLocalAddress();
        this.logger = loggerBuilder.build(ReplayFilter.class);
        this.replayerEngines = replayerEngines;
    }

    @Override
    public String getId() {
        return "org.kendar.replayer.filters.RecordFilter";
    }

    @HttpMethodFilter(
            phase = HttpFilterType.PRE_RENDER,
            pathAddress = "*",
            method = "*")
    public boolean replay(Request req, Response res) {
        if (replayerStatus.getStatus() != ReplayerState.REPLAYING) return false;

        var validAddress = false;
        for (var i = 0; i < replayerEngines.size(); i++) {
            validAddress = replayerEngines.get(i).isValidPath(req) || validAddress;
        }
        if (!validAddress) return false;
        var toReplay = "Replaying " +
                req.getProtocol() + "://" +
                req.getHost() +
                req.getPath();
        var result = replayerStatus.replay(req, res);
        if (result.isPresent()) {
            logger.info(toReplay);
        }
        return result.isPresent();
    }
}
