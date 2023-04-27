package org.kendar.mongo.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.kendar.events.EventQueue;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamRequest;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.http.annotations.multi.QueryString;
import org.kendar.mongo.config.MongoConfig;
import org.kendar.mongo.config.MongoProxy;
import org.kendar.mongo.events.MongoConfigChanged;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}", blocking = true)
public class MongoProxyApi  implements FilteringClass {
    final ObjectMapper mapper = new ObjectMapper();
    private final JsonConfiguration configuration;
    private final EventQueue eventQueue;

    public MongoProxyApi(JsonConfiguration configuration, EventQueue eventQueue) {
        this.configuration = configuration;
        this.eventQueue = eventQueue;
    }
    
    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/mongoproxies",
            method = "GET")
    @HamDoc(
            tags = {"base/proxy"},
            description = "Retrieve all configured proxies",
            responses = @HamResponse(
                    body = MongoProxy[].class
            ))
    public void getProxies(Request req, Response res) throws JsonProcessingException {
        var proxies = configuration.getConfiguration(MongoConfig.class).getProxies();
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
        res.setResponseText(mapper.writeValueAsString(proxies));
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/mongoproxies/{id}",
            method = "GET")
    @HamDoc(
            tags = {"base/proxy"},
            path = @PathParameter(key = "id"),
            query = @QueryString(key = "test", description = "Optional, if specified with true check the connection"),
            description = "Retrieve specific proxy data",
            responses = @HamResponse(
                    body = MongoProxy.class
            ))
    public void getProxy(Request req, Response res) throws JsonProcessingException, ClassNotFoundException, SQLException {
        var clone = configuration.getConfiguration(MongoConfig.class);
        var proxies = clone.getProxies();
        var id = req.getPathParameter("id");
        for (var item : proxies) {
            if (item.getId().equalsIgnoreCase(id)) {
                var test = req.getQuery("test");
                res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);
                if (test != null && test.equalsIgnoreCase("true")) {
                    try {
                        testConnection(item);
                        res.setResponseText("{}");
                    } catch (Exception ex) {
                        res.setResponseText(mapper.writeValueAsString(ex.getMessage()));
                        res.setStatusCode(500);
                    }
                } else {
                    res.setResponseText(mapper.writeValueAsString(item));
                }
                return;
            }
        }
        res.setStatusCode(404);
    }

    private void testConnection(MongoProxy item)  {
        try (MongoClient mongoClient = MongoClients.create(item.getRemote().getConnectionString())) {
            MongoDatabase database = mongoClient.getDatabase("admin");

                // Send a ping to confirm a successful connection
                Bson command = new BsonDocument("ping", new BsonInt64(1));
                Document commandResult = database.runCommand(command);
        }
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/mongoproxies/{id}",
            method = "DELETE")
    @HamDoc(
            tags = {"base/proxy"},
            path = @PathParameter(key = "id"),
            description = "Delete specific proxy"
    )
    public void removeProxy(Request req, Response res) {
        var clone = configuration.getConfiguration(MongoConfig.class).copy();
        var proxies = clone.getProxies();
        var id = req.getPathParameter("id");
        var newList = new ArrayList<MongoProxy>();
        for (var item : proxies) {
            if (item.getId().equalsIgnoreCase(id)) {
                continue;
            }
            newList.add(item);
        }
        clone.setProxies(newList);
        configuration.setConfiguration(clone);
        eventQueue.handle(new MongoConfigChanged());
        res.setStatusCode(200);
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/mongoproxies/{id}",
            method = "PUT")
    @HamDoc(
            tags = {"base/proxy"},
            description = "Modify proxy",
            path = @PathParameter(key = "id"),
            requests = @HamRequest(
                    body = MongoConfig.class
            ))
    public void updateProxy(Request req, Response res) throws JsonProcessingException {
        var cloneConf = configuration.getConfiguration(MongoConfig.class).copy();
        var proxies = cloneConf.getProxies();
        var id = req.getPathParameter("id");
        var newList = new ArrayList<MongoProxy>();
        var newData = mapper.readValue(req.getRequestText(), MongoProxy.class);

        for (var item : proxies) {
            var clone = item.copy();
            if (!clone.getId().equalsIgnoreCase(id)) {
                newList.add(clone);
                continue;
            }
            clone.setExposedPort(newData.getExposedPort());
            clone.setRemote(newData.getRemote());
            clone.setActive(newData.isActive());
            newList.add(clone);
        }
        cloneConf.setProxies(newList);
        configuration.setConfiguration(cloneConf);
        eventQueue.handle(new MongoConfigChanged());
        res.setStatusCode(200);
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/mongoproxies",
            method = "POST")
    @HamDoc(
            tags = {"base/proxy"},
            description = "Add proxy",
            requests = @HamRequest(
                    body = MongoConfig.class
            ))
    public void addProxy(Request req, Response res) throws JsonProcessingException {
        var cloneConf = configuration.getConfiguration(MongoConfig.class).copy();
        var proxies = cloneConf.getProxies();
        if (req.getRequestText() != null && !req.getRequestText().isEmpty()) {
            var newData = mapper.readValue(req.getRequestText(), MongoProxy.class);
            proxies.add(newData);
            configuration.setConfiguration(cloneConf);
        }
        eventQueue.handle(new MongoConfigChanged());
        res.setStatusCode(200);
    }
}
