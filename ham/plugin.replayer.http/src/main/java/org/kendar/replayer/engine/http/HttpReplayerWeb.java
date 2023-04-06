package org.kendar.replayer.engine.http;

import org.kendar.http.StaticWebFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}")
public class HttpReplayerWeb extends StaticWebFilter {

    @Override
    public String getDescription() {
        return "Replayer web=Http extension";
    }

    @Override
    public String getAddress() {
        return null;
    }


    public HttpReplayerWeb(FileResourcesUtils fileResourcesUtils, LoggerBuilder loggerBuilder) {
        super(fileResourcesUtils);
        Logger logger = loggerBuilder.build(HttpReplayerWeb.class);
        logger.info("Replayer server=Http Extension LOADED");
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @Override
    protected String getPath() {
        return "*web";
    }
}
