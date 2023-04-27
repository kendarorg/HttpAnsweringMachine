package org.kendar.servers.dbproxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.kendar.events.EventQueue;
import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.*;
import org.kendar.janus.JdbcDriver;
import org.kendar.janus.cmd.interfaces.JdbcCommand;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@HttpTypeFilter(hostAddress = "*",
        blocking = false)
public class DbProxyApi implements FilteringClass {
    private final JsonTypedSerializer serializer;
    private final Logger logger;
    private final Logger specialLogger;
    private final JsonConfiguration configuration;
    private final EventQueue eventQueue;
    private final ResultSetConverter resultSetConverter;
    private final ConcurrentHashMap<String, ServerData> janusEngines = new ConcurrentHashMap<>();
    private final Object syncObject = new Object();
    ObjectMapper mapper = new ObjectMapper();

    public DbProxyApi(JsonConfiguration configuration, EventQueue eventQueue, LoggerBuilder loggerBuilder,
                      PluginsInitializer pluginsInitializer, ResultSetConverter resultSetConverter) {
        this.logger = loggerBuilder.build(DbProxyApi.class);
        this.specialLogger = loggerBuilder.build(ProxyDb.class);
        this.configuration = configuration;
        this.eventQueue = eventQueue;
        this.resultSetConverter = resultSetConverter;
        this.serializer = new JsonTypedSerializer();
        eventQueue.register(this::handleConfigChange, DbProxyConfigChanged.class);
        pluginsInitializer.addSpecialLogger(ProxyDb.class.getName(), "Basic db logging (DEBUG)");
    }

    private void handleConfigChange(DbProxyConfigChanged t) {
        synchronized (syncObject) {
            janusEngines.clear();
            initialize();
        }
    }

    @PostConstruct
    public void postConstruct() {
        initialize();
    }

    private void initialize() {
        var proxyConfig = configuration.getConfiguration(DbProxyConfig.class);
        if (proxyConfig == null) return;
        for (var proxy : proxyConfig.getProxies()) {
            var id = proxy.getId().toLowerCase(Locale.ROOT);
            var remote = proxy.getRemote();
            var local = proxy.getExposed();
            var result = new ServerData();
            result.setActive(proxy.isActive());
            result.setLocal(local);
            if (result.isActive()) {
                var serverEngine = new ServerEngine(remote.getConnectionString(), remote.getLogin(), remote.getPassword());
                serverEngine.setMaxRows(500);
                result.setServerEngine(serverEngine);
            }
            logger.info("Db Proxy LOADED, from: " + local.getConnectionString() + " to " + remote.getConnectionString());
            janusEngines.put(local.getConnectionString(), result);
        }
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/jdbcproxies/test/{dbName}",
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
                            example = "password"),
                    @QueryString(
                            key = "query",
                            description = "Query",
                            example = "SELECT * FROM REPLAYER_RECORDING")
            },
            responses = @HamResponse(
                    body = String.class
            ))
    public boolean testConnection(Request req, Response res) throws Exception {
        var id = req.getPathParameter("type");
        if (id != null && id.equalsIgnoreCase("prepared")) {
            return preparedStatement(req, res);
        }
        return simpleExecution(req, res);
    }

    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/jdbcproxies/utils/modifyresultset",
            method = "POST", blocking = true)
    @HamDoc(
            tags = {"base/proxydb"},
            description = "Fill a janus resultset with data. The data field is a list[list] with the data to set. The jsonResultSet is " +
                    "a 'janus' serialized recordset (org.kendar.janus.JdbcRecordset)",
            requests = @HamRequest(body = ModifyResultSetCommand.class),
            responses = @HamResponse(
                    body = String.class
            ))
    public void modifyResultset(Request req, Response res) throws Exception {
        var newData = mapper.readValue(req.getRequestText(), ModifyResultSetCommand.class);
        var deser = serializer.newInstance();
        deser.deserialize(newData.getJsonResultSet());
        ResultSet resultSet = deser.read("result");

        HamResultSet hamResultSet = resultSetConverter.toHam(resultSet);
        var toChangeTo = new ArrayList<List<Object>>();
        ArrayNode tree = (ArrayNode) mapper.readTree(newData.getData());
        for (var sub : tree) {
            var newLine = new ArrayList<Object>();
            toChangeTo.add(newLine);
            var suba = (ArrayNode) sub;
            for (var fld : suba) {
                if (fld.getNodeType() == JsonNodeType.STRING) {
                    newLine.add(fld.textValue());
                } else if (fld.getNodeType() == JsonNodeType.NUMBER) {
                    newLine.add(fld.numberValue());
                } else if (fld.getNodeType() == JsonNodeType.BOOLEAN) {
                    newLine.add(fld.booleanValue());
                } else if (fld.getNodeType() == JsonNodeType.BINARY) {
                    newLine.add(fld.binaryValue());
                } else {
                    newLine.add(fld.textValue());
                }
            }
        }

        hamResultSet.fill(toChangeTo);
        ResultSet modified = resultSetConverter.fromHam(hamResultSet);

        var ser = serializer.newInstance();
        ser.write("result", modified);
        String modifiedSerialized = (String) ser.getSerialized();

        res.setResponseText(modifiedSerialized);
        res.setStatusCode(200);
    }

    private boolean preparedStatement(Request req, Response res) {
        var result = "";
        try {
            var id = req.getPathParameter("dBname");
            if (id == null || !janusEngines.containsKey(id) || !janusEngines.get(id).isActive()) {
                return false;
            }

            DriverManager.registerDriver(new JdbcDriver());
            var connection = DriverManager.getConnection("jdbc:janus:http://localhost/api/db/" + id);
            var statement = connection.prepareStatement(
                    "SELECT * FROM REPLAYER_RECORDING WHERE ID>?");
            statement.setInt(1, 0);
            var resultset = statement.executeQuery();
            result += "[";
            var count = 1;
            while (resultset.next()) {
                if (count > 1) result += ",";
                var partial = "{";
                for (int i = 1; i <= 100; i++) {
                    try {
                        String columnValue = resultset.getString(i);
                        if (i > 1) partial += ",";
                        partial += ("'col" + i + "'='" + columnValue + "'");
                    } catch (Exception ex) {

                    }
                }
                partial += "}";
                result += partial;
            }
            result += "]";
            connection.close();
            res.getHeaders().put("content-type", "application/json");
        } catch (Exception ex) {
            result = ex.getMessage();
            logger.error("Error", ex);
        }

        res.setResponseText(result);
        return true;
    }

    private boolean simpleExecution(Request req, Response res) {
        var result = "";
        try {
            var id = req.getPathParameter("dBname");
            if (id == null || !janusEngines.containsKey(id) || !janusEngines.get(id).isActive()) {
                return false;
            }
            var query = req.getQuery("query");
            if (query == null) {
                query = "SELECT 1=1";
            }
            DriverManager.registerDriver(new JdbcDriver());
            var connection = DriverManager.getConnection("jdbc:janus:http://localhost/api/db/" + id);
            var statement = connection.createStatement();
            var resultset = statement.executeQuery(query);
            result += "[";
            var count = 1;
            while (resultset.next()) {
                if (count > 1) result += ",";
                var partial = "{";
                for (int i = 1; i <= 100; i++) {
                    try {
                        String columnValue = resultset.getString(i);
                        if (i > 1) partial += ",";
                        partial += ("'col" + i + "'='" + columnValue + "'");
                    } catch (Exception ex) {

                    }
                }
                partial += "}";
                result += partial;
            }
            result += "]";
            connection.close();
            res.getHeaders().put("content-type", "application/json");
        } catch (Exception ex) {
            result = ex.getMessage();
            logger.error("Error", ex);
        }

        res.setResponseText(result);
        return true;
    }


    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/db/{dbName}/{targetType}/{command}/{targetId}",
            method = "POST")
    @HamDoc(
            tags = {"base/proxydb"},
            description = "Proxies db-not on connections",
            header = {
                    @Header(key = "X-Connection-Id", description = "The connection id")
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
    public boolean handleGeneral(Request req, Response res) throws Exception {
        var id = req.getPathParameter("dBname");
        if (id == null || !janusEngines.containsKey(id) || !janusEngines.get(id).isActive()) {
            return false;
        }

        var connectionId = Long.parseLong(req.getHeader("X-Connection-Id"));
        var itemId = Long.parseLong(req.getPathParameter("targetId"));
        runConnection(req, res, id, connectionId, itemId);
        return true;
    }


    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/db/{dbName}/{targetType}/{command}",
            method = "POST")
    @HamDoc(
            tags = {"base/proxydb"},
            description = "Proxies db-connections only",
            header = {
                    @Header(key = "X-Connection-Id", description = "The connection id")
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
    public boolean handleConnections(Request req, Response res) throws Exception {
        var id = req.getPathParameter("dBname");
        if (id == null || !janusEngines.containsKey(id) || !janusEngines.get(id).isActive()) {
            return false;
        }

        var connectionId = Long.parseLong(req.getHeader("X-Connection-Id"));
        runConnection(req, res, id, connectionId, connectionId);

        return true;
    }

    private void runConnection(Request req, Response res, String id, long connectionId, long itemId) throws SQLException {
        var deser = serializer.newInstance();
        deser.deserialize(req.getRequestText());
        var deserialized = (JdbcCommand) deser.read("command");

        var uuid = UUID.randomUUID();
        if (specialLogger.isTraceEnabled() || specialLogger.isDebugEnabled()) {
            specialLogger.debug(uuid + " REQ: " + req.getPath());
        }
        JdbcResult result = janusEngines.get(id).getServerEngine().execute(deserialized, connectionId, itemId);
        if (specialLogger.isTraceEnabled() || specialLogger.isDebugEnabled()) {
            if (result != null) {
                specialLogger.debug(uuid + " RES: " + result.getClass().getSimpleName());
            } else {
                specialLogger.debug(uuid + " RES: null");
            }
        }

        var ser = serializer.newInstance();
        ser.write("result", result);
        res.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.JSON);

        res.setResponseText((String) ser.getSerialized());
    }
}
