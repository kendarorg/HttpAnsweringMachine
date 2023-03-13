package org.kendar.servers.http;

import org.kendar.http.StaticWebFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}")
public class JsFilterWeb extends StaticWebFilter {

    @Override
    public String getDescription() {
        return "JsFilter web";
    }

    @Override
    public String getAddress() {
        return "plugins/jsfilter/index.html";
    }

    @Override
    public String getId() {
        return "org.kendar.replayer.ReplayerWeb";
    }

    @Override
    protected String getPath() {
        return "*web";
    }


    public JsFilterWeb(FileResourcesUtils fileResourcesUtils, LoggerBuilder loggerBuilder) {
        super(fileResourcesUtils);
        Logger logger = loggerBuilder.build(JsFilterWeb.class);
        logger.info("JsFilter server LOADED");
    }
}
