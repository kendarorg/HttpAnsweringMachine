package org.kendar.mongo.api;

import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.Header;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.mongo.JsonMongoClientHandler;
import org.kendar.mongo.compressor.CompressionHandler;
import org.kendar.mongo.config.MongoConfig;
import org.kendar.mongo.config.MongoProxy;
import org.kendar.mongo.handlers.MsgHandler;
import org.kendar.mongo.model.MongoPacket;
import org.kendar.mongo.responder.MongoResponder;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.typed.serializer.JsonTypedSerializer;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@HttpTypeFilter(hostAddress = "*",
        blocking = false)
public class MongoProxyHandlerApi implements FilteringClass {
    private final Timer timer;
    private final Logger logger;
    private final List<MsgHandler> msgHandlers;
    private final List<CompressionHandler> compressionHandlers;
    private final LoggerBuilder loggerBuilder;
    private final List<MongoResponder> responders;
    private final JsonConfiguration configuration;

    public MongoProxyHandlerApi(List<MsgHandler> msgHandlers,
                                List<CompressionHandler> compressionHandlers,
                                LoggerBuilder loggerBuilder,
                                List<MongoResponder> responders,
                                JsonConfiguration configuration){

        this.msgHandlers = msgHandlers;
        this.compressionHandlers = compressionHandlers;
        this.loggerBuilder = loggerBuilder;
        this.logger = loggerBuilder.build(MongoProxyHandlerApi.class);
        this.responders = responders;
        this.configuration = configuration;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                expireConnections();
            }
        }, 0, 30000);
    }

    private void expireConnections() {
        long time = Calendar.getInstance().getTimeInMillis();
        for(var timeouts:mongoClientHandlersTimeout.entrySet()){

            if(timeouts.getValue() < time){
                mongoClientHandlersTimeout.remove(timeouts.getKey());
                mongoClientHandlers.remove(timeouts.getKey()).close();
            }
        }
    }

    private final JsonTypedSerializer serializer = new JsonTypedSerializer();
    private final Map<String,JsonMongoClientHandler> mongoClientHandlers = new ConcurrentHashMap<>();
    private final Map<String,Long> mongoClientHandlersTimeout = new ConcurrentHashMap<>();



    @Override
    public String getId() {
        return MongoProxyHandlerApi.class.getName();
    }
    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/mongo/{port}/{dbName}/{opcode}",
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
                            description = "DbName for mongo",
                            example = "local"),
                    @PathParameter(
                            key = "port",
                            description = "The port",
                            example = "27077"),
                    @PathParameter(
                            key = "opcode",
                            description = "The op code",
                            example = "OP_MSG")
            },
            responses = @HamResponse(
                    body = String.class
            ))
    public boolean handleRequests(Request req, Response res) throws Exception {
        return handleMongoRequest(req, res);
    }


    @HttpMethodFilter(
            phase = HttpFilterType.API,
            pathAddress = "/api/mongo/{port}/{dbName}/{opcode}/{operation}",
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
                            description = "DbName for mongo",
                            example = "local"),
                    @PathParameter(
                            key = "port",
                            description = "The port",
                            example = "27077"),
                    @PathParameter(
                            key = "opcode",
                            description = "The op code",
                            example = "OP_MSG"),
                    @PathParameter(
                            key = "operation",
                            description = "The operation",
                            example = "insert")
            },
            responses = @HamResponse(
                    body = String.class
            ))
    public boolean handleOpMsgRequests(Request req, Response res) throws Exception {
        return handleMongoRequest(req, res);
    }


    private boolean handleMongoRequest(Request req, Response res) {
        var port = Integer.parseInt(req.getPathParameter("port"));
        var db = req.getPathParameter("dbName");
        var opcode = req.getPathParameter("opcode");

        var deser = serializer.newInstance();
        deser.deserialize(req.getRequestText());
        var config = configuration.getConfiguration(MongoConfig.class);
        MongoProxy founded = null;
        for(var pr:config.getProxies()){
            if(pr.getExposedPort()==port){
                founded = pr;
            }
        }
        if(founded==null){
            res.setStatusCode(404);
            return true;
        }
        var fromClient = (MongoPacket)deser.read("data");
        var globalConnectionId = req.getHeader("X-CONNECTION-ID");
        var connectionId = Integer.parseInt(req.getHeader("X-MONGO-ID"));


        if(opcode.equalsIgnoreCase("CLOSE")){
            var handler = mongoClientHandlers.get(globalConnectionId);
            handler.close();
            return true;
        }

        if(!mongoClientHandlers.containsKey(globalConnectionId)){
            mongoClientHandlers.put(globalConnectionId,new JsonMongoClientHandler(
                    founded,msgHandlers,compressionHandlers,
                    loggerBuilder,responders));

        }

        var handler = mongoClientHandlers.get(globalConnectionId);
        long time = Calendar.getInstance().getTimeInMillis()+2000;
        mongoClientHandlersTimeout.put(globalConnectionId,time);
        var serverResponse = handler.mongoRoundTrip(fromClient,connectionId);
        if(serverResponse.isFinalMessage()) {
            handler.close();
            mongoClientHandlers.remove(globalConnectionId);
        }


        //Call real server
        var ser = serializer.newInstance();
        ser.write("data",serverResponse.getResult());
        var toSend = (String)ser.getSerialized();
        res.setResponseText(toSend);
        res.getHeaders().put("content-type","application-json");
        res.setStatusCode(200);
        return true;
    }
}
