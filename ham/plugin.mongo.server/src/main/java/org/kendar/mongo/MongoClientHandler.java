package org.kendar.mongo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bson.ByteBuf;
import org.bson.ByteBufNIO;
import org.bson.io.ByteBufferBsonInput;
import org.kendar.mongo.compressor.CompressionHandler;
import org.kendar.mongo.handlers.MsgHandler;
import org.kendar.mongo.handlers.OpCodes;
import org.kendar.mongo.logging.MongoLogClient;
import org.kendar.mongo.logging.MongoLogServer;
import org.kendar.mongo.model.CompressedPacket;
import org.kendar.mongo.model.MongoPacket;
import org.kendar.mongo.responder.OpGeneralResponse;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.Sleeper;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class MongoClientHandler implements Runnable {

    private final Socket client;
    private final Logger logClient;
    private final Logger logServer;
    private final long connectionId;
    private Map<OpCodes, MsgHandler> msgHandlers;
    private Map<Integer, CompressionHandler> compressionHandlers;


    public MongoClientHandler(Socket client, List<MsgHandler> msgHandlers,
                              List<CompressionHandler> compressionHandlers,
                              LoggerBuilder loggerBuilder) {
        this.client = client;
        this.logClient = loggerBuilder.build(MongoLogClient.class);
        this.logServer = loggerBuilder.build(MongoLogServer.class);
        this.msgHandlers = msgHandlers.stream()
                .collect(Collectors.toMap(MsgHandler::getOpCode, Function.identity()));
        this.compressionHandlers =  compressionHandlers.stream()
                .collect(Collectors.toMap(CompressionHandler::getId, Function.identity()));
        this.connectionId = connectionCounter.incrementAndGet();
    }

    public static boolean readBytes(InputStream stream,byte[] buffer) throws IOException {
        int offset = 0;
        int bytesRead = 0;
        while (offset < buffer.length && (bytesRead = stream.read(buffer, offset, buffer.length - offset)) != -1) {
            offset += bytesRead;
        }
        var res = offset>0;
        if(res==false){
            return false;
        }
        return true;
    }


    //https://gist.github.com/rozza/9c94808ed5b4f1edca75
    @Override
    public void run() {
        try {

            processClient(client);

        } catch (Throwable e) {
            e.printStackTrace();
            try {
                client.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static AtomicLong connectionCounter = new AtomicLong(1);

    public static long getRequestCounter() {
        return requestCounter.incrementAndGet();
    }

    private static AtomicLong requestCounter = new AtomicLong(1);

    private static String cleanUp(MongoPacket packet){
        var ser = mapper.valueToTree(packet);
        ((ObjectNode)ser).remove("payload");
        ((ObjectNode)ser).remove("header");

        try {
            return mapper.writeValueAsString(ser);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static ObjectMapper mapper = new ObjectMapper();

    private void processClient(Socket serverSocket) throws IOException {
        try (InputStream fromClient = serverSocket.getInputStream();
             OutputStream toClient = serverSocket.getOutputStream()) {

            connectToClient();



                byte[] headerBytes = new byte[16];

                while (true) {
                    if(!readBytes(fromClient,headerBytes)){
                        Sleeper.sleep(100);
                        continue;
                    }

                    logClient.debug("===================");
                    var clientPacket = readPacketsFromStream(fromClient, headerBytes);
                    OpGeneralResponse generalResponse = mongoRoundTrip(clientPacket,connectionId);
                    logClient.debug(cleanUp(clientPacket));

                    logServer.debug(cleanUp(generalResponse.getResult()));
                    logClient.debug("===================");
                    if(generalResponse.getResult().isFinale()){
                        break;
                    }
                    toClient.write(generalResponse.getResult().serialize());

                    toClient.flush();
                    if(generalResponse.isFinalMessage()){
                        break;
                    }

                    headerBytes = new byte[16];
                }
        }
    }


    public abstract OpGeneralResponse mongoRoundTrip(MongoPacket clientPacket, long connectionId);

    protected abstract void connectToClient();

    protected MongoPacket readPacketsFromStream(InputStream inputStream, byte[] headerBytes) throws IOException {
        ByteBufferBsonInput headerInput = new ByteBufferBsonInput(
                byteBufNIOcreate(headerBytes, ByteOrder.LITTLE_ENDIAN));
        int messageLength = headerInput.readInt32();
        int requestId = headerInput.readInt32();
        int responseTo = headerInput.readInt32();
        OpCodes opCode = OpCodes.of(headerInput.readInt32());
        var packet = new MongoPacket();
        packet.setOpCode(opCode);
        if(messageLength==0 && opCode==OpCodes.OP_NONE && requestId==0 && responseTo==0){
            packet.setHeader(headerBytes);
            packet.setPayload(new byte[]{});
            return packet;
        }

        byte[] remainingBytes = new byte[messageLength - 16];

        readBytes(inputStream,remainingBytes);
        //inputStream.read(remainingBytes);
        packet.setHeader(headerBytes);
        packet.setPayload(remainingBytes);

        try {

            var uncompressedByteBuffer = byteBufNIOcreate(remainingBytes, ByteOrder.LITTLE_ENDIAN);
            ByteBufferBsonInput remainingInput = new ByteBufferBsonInput(uncompressedByteBuffer);

            if (opCode == OpCodes.OP_COMPRESSED) {
                return handleOpCompressedCode(requestId, responseTo, packet, remainingBytes, remainingInput);
            } else {
                return handleOpCode(requestId, responseTo, opCode, remainingInput, uncompressedByteBuffer, packet,remainingBytes.length);
            }
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    private MongoPacket handleOpCompressedCode(int requestId, int responseTo, MongoPacket packet, byte[] remainingBytes, ByteBufferBsonInput remainingInput) throws IOException {
        // Handle OP_COMPRESSED
        var cpacket = new CompressedPacket();
        cpacket.setHeader(packet.getHeader());
        cpacket.setPayload(packet.getPayload());
        cpacket.setRequestId(requestId);
        cpacket.setResponseTo(responseTo);
        cpacket.setOpCode(OpCodes.OP_COMPRESSED);
        cpacket.setOriginalOpCode(OpCodes.of(remainingInput.readInt32()));

        //var originalOpCode = ;
        int uncompressedSize = remainingInput.readInt32();
        byte compressorId = remainingInput.readByte();

        // Decompress remaining bytes using the appropriate compressor (e.g., Snappy, Zlib, etc.)
        byte[] decompressedBytes = decompress(remainingInput, compressorId, remainingBytes.length - 9);

        var mongoPacket = new MongoPacket<>();
        mongoPacket.setHeader(buildHeader(uncompressedSize + 16, requestId, responseTo, cpacket.getOriginalOpCode()));
        mongoPacket.setPayload(decompressedBytes);
        var byteBuffer = byteBufNIOcreate(decompressedBytes, ByteOrder.LITTLE_ENDIAN);
        ByteBufferBsonInput decompressedInput = new ByteBufferBsonInput(byteBuffer);

        var decompresedPacket = handleOpCode(requestId,responseTo,cpacket.getOriginalOpCode(), decompressedInput, byteBuffer, mongoPacket,decompressedBytes.length);
        cpacket.setCompressed(decompresedPacket);
        return cpacket;
    }


    private static byte[] buildHeader(int fullSize, int requestId, int responseTo, OpCodes originalOpCode) {
        ByteBuffer responseBuffer = ByteBuffer.allocate(16);
        responseBuffer.order(ByteOrder.LITTLE_ENDIAN);
        responseBuffer.putInt(fullSize); // messageLength
        responseBuffer.putInt(requestId); // requestID
        responseBuffer.putInt(responseTo); // responseTo
        responseBuffer.putInt(originalOpCode.getValue()); // OP_REPLY
        responseBuffer.flip();
        responseBuffer.position(0);
        return responseBuffer.array();
    }

    // Implement this method to decompress the remaining bytes using the appropriate compressor (e.g., Snappy, Zlib, etc.)
    private byte[] decompress(ByteBufferBsonInput bsonInput, byte compressorId, int compressedLength) throws IOException {
        var bb = new byte[compressedLength];
        bsonInput.readBytes(bb);
        var compressor= this.compressionHandlers.get(compressorId);
        if(compressor==null){
            System.err.println("Unknow compression "+compressorId);
        }
        return compressor.decompress(bb);
    }

    private static ByteBuf byteBufNIOcreate(byte[] headerBytes, ByteOrder byteOrder) {
        var bb = ByteBuffer.wrap(headerBytes).order(byteOrder);
        return new ByteBufNIO(bb);
    }

    private MongoPacket<?> handleOpCode(int requestId,int responseTo,OpCodes opCode, ByteBufferBsonInput bsonInput, ByteBuf byteBuffer, MongoPacket packet,
                                     int length) {
        var handler = this.msgHandlers.get(opCode);
        if(handler==null){
            System.err.println("Unknown opCode: " + opCode);
        }
        return handler.handleMsg( requestId, responseTo,bsonInput, byteBuffer, packet, length);
    }



}