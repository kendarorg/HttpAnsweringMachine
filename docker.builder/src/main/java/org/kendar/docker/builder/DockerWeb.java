package org.kendar.docker.builder;

import org.kendar.http.StaticWebFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.utils.FileResourcesUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${localhost.name}")
public class DockerWeb extends StaticWebFilter {

    @Override
    public String getDescription(){
        return "Docker generator";
    }
    @Override
    public String getAddress(){
        return "plugins/dockerbuilder/index.html";
    }

    public DockerWeb(FileResourcesUtils fileResourcesUtils) {
        super(fileResourcesUtils);
    }

    @Override
    public String getId() {
        return "org.kendar.docker.ReplayerWeb";
    }

    @Override
    protected String getPath() {
        return "*web";
    }
}
