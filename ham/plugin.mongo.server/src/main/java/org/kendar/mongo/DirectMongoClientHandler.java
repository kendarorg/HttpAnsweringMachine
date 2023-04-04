package org.kendar.mongo;

import org.kendar.mongo.compressor.CompressionHandler;
import org.kendar.mongo.handlers.MsgHandler;
import org.kendar.mongo.model.MongoPacket;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.Sleeper;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class DirectMongoClientHandler extends MongoClientHandler {

    private Socket clientSocket;
    private OutputStream toMongoDb;
    private InputStream fromMongoDb;
    private String targetIp;
    private int targetPort;

    public DirectMongoClientHandler(Socket client,
                                    List<MsgHandler> msgHandlers,
                                    List<CompressionHandler> compressionHandlers,
                                    LoggerBuilder loggerBuilder) {
        super(client, msgHandlers, compressionHandlers, loggerBuilder);
    }

    protected MongoPacket mongoRoundTrip(MongoPacket clientPacket) {
        try {
            toMongoDb.write(clientPacket.getHeader());
            toMongoDb.write(clientPacket.getPayload());
            toMongoDb.flush();
            byte[] mongoHeaderBytes = new byte[16];
            while(!readBytes(fromMongoDb, mongoHeaderBytes)){
                Sleeper.sleep(100);
            }
            return readPacketsFromStream(fromMongoDb, mongoHeaderBytes);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void connectToClient() {
        try {
            clientSocket = new Socket(targetIp, targetPort);
            toMongoDb = clientSocket.getOutputStream();
            fromMongoDb = clientSocket.getInputStream();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setTarget(String targetIp, int targetPort) {

        this.targetIp = targetIp;
        this.targetPort = targetPort;
    }
}