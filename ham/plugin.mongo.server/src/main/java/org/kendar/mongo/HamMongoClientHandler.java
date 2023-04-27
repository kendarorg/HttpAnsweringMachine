package org.kendar.mongo;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.events.EventQueue;
import org.kendar.events.ExecuteLocalRequest;
import org.kendar.mongo.compressor.CompressionHandler;
import org.kendar.mongo.events.MongoConfigChanged;
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
import java.util.*;

public class HamMongoClientHandler extends MongoClientHandler {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final String connectionId;
    private final EventQueue eventQueue;
    private final int localPort;

    private final JsonTypedSerializer serializer;
    private long longConnectionId;

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
        eventQueue.register(this::handleConfigChange, MongoConfigChanged.class);
    }

    private void handleConfigChange(MongoConfigChanged t) {
        this.close();
    }

    @Override
    public void close() {
        try {
            var ser = serializer.newInstance();
            var mp = new MongoPacket<>();
            mp.setOpCode(OpCodes.OP_NONE);
            mongoRoundTrip(mp, this.longConnectionId);
        } catch (Exception e) {

        }
    }


    @Override
    public OpGeneralResponse mongoRoundTrip(MongoPacket clientPacket, long connectionId) {

        try {
            longConnectionId = connectionId;
            String db = "admin";
            var isHelloPacket = isHelloPacket(clientPacket);
            if (clientPacket.getOpCode() == OpCodes.OP_MSG) {
                db = getDb((MsgPacket) clientPacket);
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
            req.setProtocol("http");
            req.setMethod("POST");
            req.setHost("127.0.0.1");
            req.setHeaders(new HashMap<>());
            req.getHeaders().put("X-CONNECTION-ID", this.connectionId);
            req.getHeaders().put("X-MONGO-ID", "" + connectionId);
            req.setPort(80);
            String command = "NONE";
            if (clientPacket instanceof MsgPacket) {
                var msg = (MsgPacket) clientPacket;
                var payload = (MsgDocumentPayload) msg.getPayloads().get(0);
                //System.out.println(payload.getJson());
                var jsonPayload = mapper.readTree(payload.getJson());
                List<String> keys = new ArrayList<>();
                Iterator<String> iterator = jsonPayload.fieldNames();
                iterator.forEachRemaining(keys::add);
                command = keys.get(0);
                //System.out.println("SENDING "+command);
                req.setPath(
                        String.format("/api/mongo/%d/%s/%s/%s", localPort, db,
                                clientPacket.getOpCode(), command));

            } else if (clientPacket instanceof QueryPacket) {
                var msg = (QueryPacket) clientPacket;
                var jsonPayload = mapper.readTree(msg.getJson());
                List<String> keys = new ArrayList<>();
                Iterator<String> iterator = jsonPayload.fieldNames();
                iterator.forEachRemaining(keys::add);
                command = keys.get(0);
                //System.out.println("SENDING "+command);
                req.setPath(
                        String.format("/api/mongo/%d/%s/%s/%s", localPort, db,
                                clientPacket.getOpCode(), command));

            } else {
                command = clientPacket.getClass().getName();
                //System.out.println("SENDING UNKNOWN "+command);
                req.setPath(
                        String.format("/api/mongo/%d/%s/%s", localPort, db,
                                clientPacket.getOpCode()));
            }

            var event = new ExecuteLocalRequest();
            event.setRequest(req);
            var result = eventQueue.execute(event, Response.class);
            var response = result.getResponseText();
            //System.out.println("RECEIVED "+command);
            var deser = serializer.newInstance();
            deser.deserialize(response);
            var serverPacket = (MongoPacket) deser.read("data");
            return new OpGeneralResponse(serverPacket, false);
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
                    var db = (JsonNode) jsonTree.get("$db");
                    return db.asText();
                }

            }
        } catch (Exception ex) {

        }
        return "admin";
    }

    private boolean isHelloPacket(MongoPacket clientPacket) {
        try {
            if (clientPacket.getOpCode() == OpCodes.OP_QUERY) {
                var opQuery = (QueryPacket) clientPacket;
                if (opQuery.getFullCollectionName().equalsIgnoreCase("admin.$cmd")) {
                    var jsonTree = mapper.readTree(opQuery.getJson());
                    var helloOk = (JsonValue) jsonTree.get("helloOk");
                    if (helloOk.value()) {
                        return true;
                    }

                }
            }
        } catch (Exception ex) {

        }
        return false;
    }

    @Override
    protected void connectToClient() {

    }
}
