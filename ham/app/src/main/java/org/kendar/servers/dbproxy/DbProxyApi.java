package org.kendar.servers.dbproxy;

import org.kendar.dns.configurations.DnsConfig;
import org.kendar.events.Event;
import org.kendar.events.EventQueue;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.Example;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.http.annotations.multi.QueryString;
import org.kendar.janus.JdbcDriver;
import org.kendar.janus.cmd.Exec;
import org.kendar.janus.cmd.JdbcCommand;
import org.kendar.janus.results.JdbcResult;
import org.kendar.janus.results.ObjectResult;
import org.kendar.janus.serialization.JsonTypedSerializer;
import org.kendar.janus.server.ServerEngine;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}",
        blocking = true)
public class DbProxyApi implements FilteringClass {
    private final JsonTypedSerializer serializer;
    private final Logger logger;
    private JsonConfiguration configuration;
    private EventQueue eventQueue;
    private ConcurrentHashMap<String,ServerData> janusEngines = new ConcurrentHashMap<>();

    public DbProxyApi(JsonConfiguration configuration, EventQueue eventQueue, LoggerBuilder loggerBuilder){
        this.logger = loggerBuilder.build(DbProxyApi.class);
        this.configuration = configuration;
        this.eventQueue = eventQueue;
        this.serializer = new JsonTypedSerializer();
        eventQueue.register(this::handleConfigChange,DbProxyConfigChanged.class);
    }

    private Object syncObject = new Object();
    private  void handleConfigChange(DbProxyConfigChanged t) {
        synchronized (syncObject){
            janusEngines.clear();
            initialize();
        }
    }

    @PostConstruct
    public void postConstruct(){
        initialize();
    }

    private void initialize() {
        var proxyConfig = configuration.getConfiguration(DbProxyConfig.class);
        if(proxyConfig==null)return;
        for(var proxy:proxyConfig.getProxies()){
            var id = proxy.getId().toLowerCase(Locale.ROOT);
            var remote = proxy.getRemote();
            var local = proxy.getExposed();
            var result = new ServerData();
            result.setActive(proxy.isActive());
            result.setLocal(local);
            if(result.isActive()) {
                var serverEngine = new ServerEngine(remote.getConnectionString(), remote.getLogin(), remote.getPassword());
                result.setServerEngine(serverEngine);
            }
            logger.info("Db Proxy LOADED, from: " + local.getConnectionString()+" to "+remote.getConnectionString());
            janusEngines.put(local.getConnectionString(),result);
        }
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/db/{dbName}/{connectionId}/",
            method = "GET")
    @HamDoc(
            tags = {"base/proxydb"},
            description = "Proxies db",
            path = {
                    @PathParameter(
                            key = "dbName",
                            description = "DbName on confix",
                            example = "local")
            },
            query = {
                    @QueryString(
                            key = "login",
                            description = "login",
                            example = "login"),
                    @QueryString(
                            key = "password",
                            description = "password",
                            example = "password")
            },
            responses = @HamResponse(
                    body = String.class
            ))
    public void testConnection(Request req, Response res) throws Exception {

        DriverManager.registerDriver(new JdbcDriver());
        var connection = DriverManager.getConnection("jdbc:janus:http://localhost/db/local");
        connection.createStatement().execute("SELECT 1=1");
    }


    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/db/{dbName}/{connectionId}/{itemId}",
            method = "POST")
    @HamDoc(
            tags = {"base/proxydb"},
            description = "Proxies db",
            path = {
                    @PathParameter(
                            key = "dbName",
                            description = "DbName on confix",
                            example = "local"),
                    @PathParameter(
                            key = "connectionId",
                            description = "Connecction Id",
                            example = "22"),

                    @PathParameter(
                            key = "itemId",
                            description = "Jdbc Object Id",
                            example = "77")
            },
            responses = @HamResponse(
                    body = String.class
            ))
    public void handle(Request req, Response res) throws Exception {
        var id = req.getPathParameter("dBname");
        if(id==null ||!janusEngines.containsKey(id)||!janusEngines.get(id).isActive()){
            throw new Exception("Db not existing or inactive");
        }

        var connectionId = Long.parseLong(req.getPathParameter("connectionId"));
        var itemId = Long.parseLong(req.getPathParameter("itemId"));
        var deser = serializer.newInstance();
        deser.deserialize(req.getRequestText());
        var deserialized = (JdbcCommand) deser.read("command");
        JdbcResult result = janusEngines.get(id).getServerEngine().execute(deserialized, connectionId, itemId);

        var ser = serializer.newInstance();
        ser.write("result", result);
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);

        res.setResponseText((String) ser.getSerialized());
    }
}
