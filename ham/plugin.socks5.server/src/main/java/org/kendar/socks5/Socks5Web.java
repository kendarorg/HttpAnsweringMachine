package org.kendar.socks5;

import org.kendar.http.StaticWebFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}")
public class Socks5Web extends StaticWebFilter {

    public Socks5Web(FileResourcesUtils fileResourcesUtils, LoggerBuilder loggerBuilder) {
        super(fileResourcesUtils);
        //Logger logger = loggerBuilder.build(Socks5Web.class);
    }

    @Override
    public String getDescription() {
        return "Socks5 proxy";
    }

    @Override
    public String getAddress() {
        return "plugins/socks5/index.html";
    }

    @Override
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    protected String getPath() {
        return "*web";
    }
}
