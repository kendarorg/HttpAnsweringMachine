package org.kendar.servers.dbproxy;

import org.kendar.events.EventQueue;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.Header;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.http.annotations.multi.QueryString;
import org.kendar.janus.JdbcDriver;
import org.kendar.janus.cmd.JdbcCommand;
import org.kendar.janus.results.JdbcResult;
import org.kendar.janus.serialization.JsonTypedSerializer;
import org.kendar.janus.server.ServerEngine;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.PluginsInitializer;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.ConstantsHeader;
import org.kendar.utils.ConstantsMime;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@HttpTypeFilter(hostAddress = "${global.localAddress}",
        blocking = true)
public class DbProxyApi implements FilteringClass {
    private final JsonTypedSerializer serializer;
    private final Logger logger;
    private final Logger specialLogger;
    private JsonConfiguration configuration;
    private EventQueue eventQueue;
    private ConcurrentHashMap<String,ServerData> janusEngines = new ConcurrentHashMap<>();

    public DbProxyApi(JsonConfiguration configuration, EventQueue eventQueue, LoggerBuilder loggerBuilder,
                      PluginsInitializer pluginsInitializer){
        this.logger = loggerBuilder.build(DbProxyApi.class);
        this.specialLogger = loggerBuilder.build(ProxyDb.class);
        this.configuration = configuration;
        this.eventQueue = eventQueue;
        this.serializer = new JsonTypedSerializer();
        eventQueue.register(this::handleConfigChange,DbProxyConfigChanged.class);
        pluginsInitializer.addSpecialLogger(ProxyDb.class.getName(), "Basic db logging (DEBUG)");
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
            pathAddress = "/api/db/{dbName}",
            method = "GET")
    @HamDoc(
            tags = {"base/proxydb"},
            description = "Test the connection with a db",
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
        var id = req.getPathParameter("dBname");
        var query = req.getQuery("query");
        if(query==null){
            query = "SELECT 1=1";
        }
        DriverManager.registerDriver(new JdbcDriver());
        var connection = DriverManager.getConnection("jdbc:janus:http://localhost/api/db/"+id);
        var statement = connection.createStatement();
        var resultset = statement.executeQuery(query);
        var next = resultset.next();
        System.out.println("TEST");
    }


    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/db/{dbName}/{targetType}/{command}/{targetId}",
            method = "POST")
    @HamDoc(
            tags = {"base/proxydb"},
            description = "Proxies db-not on connections",
            header = {
              @Header(key="X-Connection-Id",description = "The connection id")
            },
            path = {
                    @PathParameter(
                            key = "dbName",
                            description = "DbName on confix",
                            example = "local"),
                    @PathParameter(
                            key = "targetType",
                            description = "The type of object for invocation",
                            example = "ResultSet"),
                    @PathParameter(
                            key = "command",
                            description = "the command to execute",
                            example = "getResultSetMetaData"),

                    @PathParameter(
                            key = "targetId",
                            description = "Jdbc Id of the target",
                            example = "77")
            },
            responses = @HamResponse(
                    body = String.class
            ))
    public void handleGeneral(Request req, Response res) throws Exception {
        var id = req.getPathParameter("dBname");
        if(id==null ||!janusEngines.containsKey(id)||!janusEngines.get(id).isActive()){
            throw new Exception("Db not existing or inactive");
        }

        var connectionId = Long.parseLong(req.getHeader("X-Connection-Id"));
        var itemId = Long.parseLong(req.getPathParameter("targetId"));
        runConnection(req, res, id, connectionId, itemId);
    }




    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/db/{dbName}/{targetType}/{command}",
            method = "POST")
    @HamDoc(
            tags = {"base/proxydb"},
            description = "Proxies db-connections only",
            header = {
                    @Header(key="X-Connection-Id",description = "The connection id")
            },
            path = {
                    @PathParameter(
                            key = "dbName",
                            description = "DbName on confix",
                            example = "local"),
                    @PathParameter(
                            key = "targetType",
                            description = "The type of object for invocation",
                            example = "ResultSet"),
                    @PathParameter(
                            key = "command",
                            description = "the command to execute",
                            example = "getResultSetMetaData")
            },
            responses = @HamResponse(
                    body = String.class
            ))
    public void handleConnections(Request req, Response res) throws Exception {
        var id = req.getPathParameter("dBname");
        if(id==null ||!janusEngines.containsKey(id)||!janusEngines.get(id).isActive()){
            throw new Exception("Db not existing or inactive");
        }

        var connectionId = Long.parseLong(req.getHeader("X-Connection-Id"));
        runConnection(req, res, id, connectionId, connectionId);
    }

    private void runConnection(Request req, Response res, String id, long connectionId, long itemId) throws SQLException {
        var deser = serializer.newInstance();
        deser.deserialize(req.getRequestText());
        var deserialized = (JdbcCommand) deser.read("command");

        var uuid = UUID.randomUUID();
        if(specialLogger.isTraceEnabled()||specialLogger.isDebugEnabled()) {
            specialLogger.debug(uuid+" REQ: "+ req.getPath());
        }
        JdbcResult result = janusEngines.get(id).getServerEngine().execute(deserialized, connectionId, itemId);
        if(specialLogger.isTraceEnabled()||specialLogger.isDebugEnabled()) {
            if(result!=null){
                specialLogger.debug(uuid+" RES: "+result.getClass().getSimpleName());
            }else{
                specialLogger.debug(uuid+" RES: null");
            }
        }

        var ser = serializer.newInstance();
        ser.write("result", result);
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);

        res.setResponseText((String) ser.getSerialized());
    }
}
