package org.kendar.mongo;

import org.kendar.http.StaticWebFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}")
public class MongoWeb extends StaticWebFilter {

    @Override
    public String getDescription() {
        return "Mongo web";
    }

    @Override
    public String getAddress() {
        return "plugins/mongo/index.html";
    }


    public MongoWeb(FileResourcesUtils fileResourcesUtils, LoggerBuilder loggerBuilder) {
        super(fileResourcesUtils);
        Logger logger = loggerBuilder.build(MongoWeb.class);
        logger.info("Mongo server LOADED");
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
