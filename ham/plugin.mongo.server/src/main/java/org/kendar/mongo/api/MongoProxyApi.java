package org.kendar.mongo.api;

import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.Header;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "*",
        blocking = false)
public class MongoProxyApi implements FilteringClass {
    @Override
    public String getId() {
        return MongoProxyApi.class.getName();
    }
    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/mongo/{port}/{dbName}",
            method = "POST")
    @HamDoc(
            tags = {"base/proxymongo"},
            description = "Proxies mongo-not on connections",
            header = {
                    @Header(key = "X-Connection-Id", description = "The connection id")
            },
            path = {
                    @PathParameter(
                            key = "dbName",
                            description = "DbName on confix",
                            example = "local"),
                    @PathParameter(
                            key = "port",
                            description = "The the port",
                            example = "27077"),
            },
            responses = @HamResponse(
                    body = String.class
            ))
    public boolean handleCommands(Request req, Response res) throws Exception {
        return true;
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/mongo/{port}",
            method = "POST")
    @HamDoc(
            tags = {"base/proxymongo"},
            description = "Proxies mongo-not on connections",
            header = {
                    @Header(key = "X-Connection-Id", description = "The connection id")
            },
            path = {
                    @PathParameter(
                            key = "port",
                            description = "The the port",
                            example = "27077"),
            },
            responses = @HamResponse(
                    body = String.class
            ))
    public boolean handleConnection(Request req, Response res) throws Exception {
        return true;
    }
}
