package org.kendar.servers;

import org.kendar.http.StaticWebFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.utils.FileResourcesUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${localhost.name}")
public class MainWebSite extends StaticWebFilter {
    public MainWebSite(FileResourcesUtils fileResourcesUtils) {
        super(fileResourcesUtils);
    }

    @Override
    public String getId() {
        return "org.kendar.docker.MainWebSite";
    }

    @Override
    protected String getPath() {
        return "*web";
    }
}
