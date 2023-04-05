package org.kendar.replayer.engine.db;

import org.kendar.http.StaticWebFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}")
public class JDbcReplayerWeb extends StaticWebFilter {

    @Override
    public String getDescription() {
        return "Replayer web=Jdbc extension";
    }

    @Override
    public String getAddress() {
        return null;
    }


    public JDbcReplayerWeb(FileResourcesUtils fileResourcesUtils, LoggerBuilder loggerBuilder) {
        super(fileResourcesUtils);
        Logger logger = loggerBuilder.build(JDbcReplayerWeb.class);
        logger.info("Replayer server=Jdbc Extension LOADED");
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
