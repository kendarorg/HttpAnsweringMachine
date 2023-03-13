package org.kendar.replayer;

import org.kendar.http.StaticWebFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}")
public class ReplayerWeb extends StaticWebFilter {

    @Override
    public String getDescription() {
        return "Replayer web";
    }

    @Override
    public String getAddress() {
        return "plugins/recording/index.html";
    }


    public ReplayerWeb(FileResourcesUtils fileResourcesUtils, LoggerBuilder loggerBuilder) {
        super(fileResourcesUtils);
        Logger logger = loggerBuilder.build(ReplayerWeb.class);
        logger.info("Replayer server LOADED");
    }

    @Override
    public String getId() {
        return "org.kendar.replayer.ReplayerWeb";
    }

    @Override
    protected String getPath() {
        return "*web";
    }
}
