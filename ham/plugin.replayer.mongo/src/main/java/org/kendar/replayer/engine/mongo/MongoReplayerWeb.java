package org.kendar.replayer.engine.mongo;

import org.kendar.http.StaticWebFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}")
public class MongoReplayerWeb extends StaticWebFilter {

    public MongoReplayerWeb(FileResourcesUtils fileResourcesUtils, LoggerBuilder loggerBuilder) {
        super(fileResourcesUtils);
        Logger logger = loggerBuilder.build(MongoReplayerWeb.class);
        logger.info("Replayer server=Mongo Extension LOADED");
    }

    @Override
    public String getDescription() {
        return "Replayer web=Mongo extension";
    }

    @Override
    public String getAddress() {
        return null;
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
