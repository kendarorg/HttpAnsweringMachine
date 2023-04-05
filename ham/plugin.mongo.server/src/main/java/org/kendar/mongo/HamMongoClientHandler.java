package org.kendar.mongo;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.events.EventQueue;
import org.kendar.events.ExecuteLocalRequest;
import org.kendar.mongo.compressor.CompressionHandler;
import org.kendar.mongo.handlers.MsgHandler;
import org.kendar.mongo.handlers.OpCodes;
import org.kendar.mongo.model.MongoPacket;
import org.kendar.mongo.model.MsgPacket;
import org.kendar.mongo.model.QueryPacket;
import org.kendar.mongo.model.payloads.MsgDocumentPayload;
import org.kendar.mongo.responder.OpGeneralResponse;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.typed.serializer.JsonTypedSerializer;
import org.kendar.utils.LoggerBuilder;

import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class HamMongoClientHandler extends MongoClientHandler {
    private static ObjectMapper mapper = new ObjectMapper();
    private final String connectionId;
    private EventQueue eventQueue;
    private int localPort;

    private final JsonTypedSerializer serializer;

    public HamMongoClientHandler(Socket client,
                                 List<MsgHandler> msgHandlers,
                                 List<CompressionHandler> compressionHandlers,
                                 LoggerBuilder loggerBuilder,
                                 EventQueue eventQueue,
                                 int localPort) {
        super(client, msgHandlers, compressionHandlers, loggerBuilder);
        this.eventQueue = eventQueue;
        this.localPort = localPort;
        this.serializer = new JsonTypedSerializer();
        this.connectionId = UUID.randomUUID().toString();
    }

    @Override
    public OpGeneralResponse mongoRoundTrip(MongoPacket clientPacket, long connectionId) {

        try {
            String db = "admin";
            var isHelloPacket = isHelloPacket(clientPacket);
            if(clientPacket.getOpCode()==OpCodes.OP_MSG){
                db = getDb((MsgPacket)clientPacket);
            }
            clientPacket.setPayload(null);
            clientPacket.setHeader(null);


            //Find the destination port
            //Find (eventually) the db
            //Serialize the packet
            var ser = serializer.newInstance();
            ser.write("data", clientPacket);
            var toSend = (String) ser.getSerialized();
            //Send it via webApi
            var req = new Request();
            req.setRequestText(toSend);
            req.setMethod("POST");
            req.setHost("127.0.0.1");
            req.setHeaders(new HashMap<>());
            req.getHeaders().put("X-CONNECTION-ID", this.connectionId);
            req.getHeaders().put("X-MONGO-ID", ""+connectionId);
            req.setPort(80);
            req.setPath(
                        String.format("/api/mongo/%d/%s", localPort,db));

            var event = new ExecuteLocalRequest();
            event.setRequest(req);
            var result = eventQueue.execute(event, Response.class);
            var response = result.getResponseText();
            var deser = serializer.newInstance();
            deser.deserialize(response);
            var serverPacket = (MongoPacket) deser.read("data");
            return new OpGeneralResponse(serverPacket,false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getDb(MsgPacket message) {
        try {
            for (var payload : message.getPayloads()) {
                if (payload instanceof MsgDocumentPayload) {
                    var jsonTree = mapper.readTree(
                            ((MsgDocumentPayload) payload).getJson());
                    var db = (JsonNode)jsonTree.get("$db");
                    return db.asText();
                }

            }
        }catch (Exception ex){

        }
        return "admin";
    }

    private boolean isHelloPacket(MongoPacket clientPacket) {
        try {
            if (clientPacket.getOpCode() == OpCodes.OP_QUERY) {
                var opQuery = (QueryPacket) clientPacket;
                if(opQuery.getFullCollectionName().equalsIgnoreCase("admin.$cmd")){
                    var jsonTree = mapper.readTree(opQuery.getJson());
                    var helloOk = (JsonValue)jsonTree.get("helloOk");
                    if(helloOk.value()){
                        return true;
                    }

                }
            }
        }catch (Exception ex){

        }
        return false;
    }

    @Override
    protected void connectToClient() {

    }
}
