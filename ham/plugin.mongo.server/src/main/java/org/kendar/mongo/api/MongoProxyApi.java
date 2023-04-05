package org.kendar.mongo.api;

import org.kendar.http.FilteringClass;
import org.kendar.http.HttpFilterType;
import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.http.annotations.multi.HamResponse;
import org.kendar.http.annotations.multi.Header;
import org.kendar.http.annotations.multi.PathParameter;
import org.kendar.janus.serialization.JsonTypedSerializer;
import org.kendar.mongo.JsonMongoClientHandler;
import org.kendar.mongo.compressor.CompressionHandler;
import org.kendar.mongo.handlers.MsgHandler;
import org.kendar.mongo.model.MongoPacket;
import org.kendar.mongo.responder.MongoResponder;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@HttpTypeFilter(hostAddress = "*",
        blocking = false)
public class MongoProxyApi implements FilteringClass {
    private final Timer timer;
    private List<MsgHandler> msgHandlers;
    private List<CompressionHandler> compressionHandlers;
    private LoggerBuilder loggerBuilder;
    private List<MongoResponder> responders;

    public MongoProxyApi(List<MsgHandler> msgHandlers,
                         List<CompressionHandler> compressionHandlers,
                         LoggerBuilder loggerBuilder,
                         List<MongoResponder> responders){

        this.msgHandlers = msgHandlers;
        this.compressionHandlers = compressionHandlers;
        this.loggerBuilder = loggerBuilder;
        this.responders = responders;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                expireConnections();
            }
        }, 0, 500);
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
    private Map<String,JsonMongoClientHandler> mongoClientHandlers = new ConcurrentHashMap<>();
    private Map<String,Long> mongoClientHandlersTimeout = new ConcurrentHashMap<>();



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
                            description = "DbName for mongo",
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
        return handleMongoCommand(req, res);
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
        return handleMongoCommand(req, res);
    }



    private boolean handleMongoCommand(Request req, Response res) {
        var deser = serializer.newInstance();
        deser.read(req.getRequestText());
        var port = Integer.parseInt(req.getPathParameter("port"));
        var db = Integer.parseInt(req.getPathParameter("port"));
        var fromClient = (MongoPacket)deser.read("data");
        var globalConnectionId = req.getHeader("X-CONNECTION-ID");
        var connectionId = Integer.parseInt(req.getHeader("X-MONGO-ID"));

        if(!mongoClientHandlers.containsKey(globalConnectionId)){
            mongoClientHandlers.put(globalConnectionId,new JsonMongoClientHandler(
                    null,msgHandlers,compressionHandlers,
                    loggerBuilder,responders));

        }

        var handler = mongoClientHandlers.get(globalConnectionId);
        long time = Calendar.getInstance().getTimeInMillis()+2000;
        mongoClientHandlersTimeout.put(globalConnectionId,time);
        var serverResponse = handler.mongoRoundTrip(fromClient,connectionId);
        if(serverResponse.isFinalMessage()){
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
