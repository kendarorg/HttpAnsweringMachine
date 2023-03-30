package org.kendar.mongo;

import com.github.luben.zstd.ZstdInputStream;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoNamespace;
import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.io.BasicOutputBuffer;
import org.bson.io.ByteBufferBsonInput;
import org.xerial.snappy.Snappy;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.InflaterInputStream;

public class MongoClientHandler implements Runnable {
    private static Codec<Document> DOCUMENT_CODEC = new DocumentCodec();

    private final InputStream input;
    private Socket client;

    public MongoClientHandler(Socket client) {
        this.client = client;
        try {
            this.input =  client.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void readBytes(InputStream stream,byte[] buffer) throws IOException {
        int offset = 0;
        int bytesRead = 0;
        while (offset < buffer.length && (bytesRead = stream.read(buffer, offset, buffer.length - offset)) != -1) {
            offset += bytesRead;
        }
    }

    public boolean running = true;

    //https://gist.github.com/rozza/9c94808ed5b4f1edca75
    @Override
    public void run() {
        try {
            while (running) {
                //Read from client
                processClient(client);
                //Send to server
                //Read from server
                //Send back to client
            }
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

    private static int msgcounter = 0;
    private static void processClient(Socket serverSocket) throws IOException {
        try (InputStream fromClient = serverSocket.getInputStream();
             OutputStream toClient = serverSocket.getOutputStream()) {

            try (var clientSocket = new Socket("localhost", 27017)) {

                var toMongoDb = clientSocket.getOutputStream();
                var fromMongoDb = clientSocket.getInputStream();
                byte[] headerBytes = new byte[16];
                byte[] mongoHeaderBytes = new byte[16];
                while (true) {
                    readBytes(fromClient,headerBytes);
                    msgcounter++;
                    System.out.println("===================");
                    System.out.println("==FROM CLIENT");
                    var clientPacket = readPacketsFromStream(fromClient, headerBytes);
                    toMongoDb.write(clientPacket.getHeader());
                    toMongoDb.write(clientPacket.getPayload());
                    toMongoDb.flush();
                    readBytes(fromMongoDb,mongoHeaderBytes);
                        System.out.println("==FROM SERVER");
                        var mongoPacket = readPacketsFromStream(fromMongoDb, mongoHeaderBytes);
                        toClient.write(mongoPacket.getHeader());
                        toClient.write(mongoPacket.getPayload());
                        toClient.flush();
                        System.out.println("===================");

                    headerBytes = new byte[16];
                    mongoHeaderBytes = new byte[16];


                    //sendIsMasterReply(outputStream,requestId);
                    // Send an acknowledgement to the client
                /*BasicOutputBuffer ackBuffer = new BasicOutputBuffer();
                ackBuffer.writeInt(16); // messageLength
                ackBuffer.writeInt(requestId); // requestId
                ackBuffer.writeInt(responseTo); // responseTo
                ackBuffer.writeInt(OpCodes.OP_REPLY); // opCode

                outputStream.write(ackBuffer.toByteArray());*/
                }
            }
        }
    }
    private static boolean testit=true;
    private static MongoPacket readPacketsFromStream(InputStream inputStream, byte[] headerBytes) throws IOException {
        ByteBufferBsonInput headerInput = new ByteBufferBsonInput(
                ByteBufNIOcreate(headerBytes, ByteOrder.LITTLE_ENDIAN));
        int messageLength = headerInput.readInt32();
        int requestId = headerInput.readInt32();
        int responseTo = headerInput.readInt32();
        int opCode = headerInput.readInt32();
        var packet = new MongoPacket(messageLength, requestId, responseTo, opCode);

        byte[] remainingBytes = new byte[messageLength - 16];

        readBytes(inputStream,remainingBytes);
        //inputStream.read(remainingBytes);
        packet.setHeader(headerBytes);
        packet.setPayload(remainingBytes);
        System.out.println("=>"+opCode+"  "+remainingBytes.length);
        if(!testit) {
            return packet;
        }

        try {

            var uncompressedByteBuffer = ByteBufNIOcreate(remainingBytes, ByteOrder.LITTLE_ENDIAN);
            ByteBufferBsonInput remainingInput = new ByteBufferBsonInput(uncompressedByteBuffer);

            if (opCode == OpCodes.OP_COMPRESSED) {
                // Handle OP_COMPRESSED
                int originalOpCode = remainingInput.readInt32();
                int uncompressedSize = remainingInput.readInt32();
                byte compressorId = remainingInput.readByte();

                // Decompress remaining bytes using the appropriate compressor (e.g., Snappy, Zlib, etc.)
                byte[] decompressedBytes = decompress(remainingInput, compressorId, remainingBytes.length - 9);

                packet = new MongoPacket(uncompressedSize + 16, requestId, responseTo, originalOpCode);
                packet.setHeader(buildHeader(uncompressedSize + 16, requestId, responseTo, originalOpCode));
                packet.setPayload(decompressedBytes);
                var byteBuffer = ByteBufNIOcreate(decompressedBytes, ByteOrder.LITTLE_ENDIAN);
                ByteBufferBsonInput decompressedInput = new ByteBufferBsonInput(byteBuffer);

                handleOpCode(originalOpCode, decompressedInput, byteBuffer, packet);
            } else {
                handleOpCode(opCode, remainingInput, uncompressedByteBuffer, packet);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return packet;
    }

    private static byte[] buildHeader(int fullSize, int requestId, int responseTo, int originalOpCode) {
        ByteBuffer responseBuffer = ByteBuffer.allocate(16);
        responseBuffer.order(ByteOrder.LITTLE_ENDIAN);
        responseBuffer.putInt(fullSize); // messageLength
        responseBuffer.putInt(requestId); // requestID
        responseBuffer.putInt(responseTo); // responseTo
        responseBuffer.putInt(originalOpCode); // OP_REPLY
        responseBuffer.flip();
        responseBuffer.position(0);
        return responseBuffer.array();
    }

    private static ByteBuf ByteBufNIOcreate(byte[] headerBytes, ByteOrder byteOrder) {
        var bb = ByteBuffer.wrap(headerBytes).order(byteOrder);
        return new ByteBufNIO(bb);
    }

    private static void sendIsMasterReply(OutputStream clientChannel, int requestId) {
        BsonDocument responseDoc = new BsonDocument()
                .append("ismaster", BsonDocumentHelper.fromBoolean(true))
                .append("maxBsonObjectSize", BsonDocumentHelper.fromInt32(16777216))
                .append("maxMessageSizeBytes", BsonDocumentHelper.fromInt32(48000000))
                .append("maxWriteBatchSize", BsonDocumentHelper.fromInt32(1000))
                .append("localTime", BsonDocumentHelper.fromDateTime(System.currentTimeMillis()))
                .append("logicalSessionTimeoutMinutes", BsonDocumentHelper.fromInt32(30))
                .append("minWireVersion", BsonDocumentHelper.fromInt32(0))
                .append("maxWireVersion", BsonDocumentHelper.fromInt32(9))
                .append("readOnly", BsonDocumentHelper.fromBoolean(false));

        BasicOutputBuffer outputBuffer = new BasicOutputBuffer();
        BsonBinaryWriter writer = new BsonBinaryWriter(outputBuffer);

        BsonDocumentCodec documentCodec = new BsonDocumentCodec();
        documentCodec.encode(writer, responseDoc, EncoderContext.builder().build());

        ByteBuffer responseBuffer = ByteBuffer.allocate(16 + outputBuffer.size()*2);
        responseBuffer.order(ByteOrder.LITTLE_ENDIAN);

        int responseFlags = 8; // CursorNotFound=0, QueryFailure=0, ShardConfigStale=0, AwaitCapable=1
        int startingFrom = 0;
        int numberReturned = 1;

        responseBuffer.putInt(16 + outputBuffer.size()); // messageLength
        responseBuffer.putInt(requestId); // requestID
        responseBuffer.putInt(0); // responseTo
        responseBuffer.putInt(1); // OP_REPLY
        responseBuffer.putInt(responseFlags); // responseFlags
        responseBuffer.putLong(0L); // cursorID
        responseBuffer.putInt(startingFrom); // startingFrom
        responseBuffer.putInt(numberReturned); // numberReturned

        responseBuffer.put(outputBuffer.getInternalBuffer(), 0, outputBuffer.size());
        responseBuffer.flip();

        try {
            clientChannel.write(responseBuffer.array());
        } catch (IOException e) {
            System.err.println("Error sending isMaster reply: " + e.getMessage());
        }
    }

    private static void handleOpCode(int opCode, ByteBufferBsonInput bsonInput, ByteBuf byteBuffer, MongoPacket packet) {
        switch (opCode) {
            case 1: // OP_REPLY
                handleReply(bsonInput,byteBuffer,packet);
                break;
            case 2013: // OP_MSG
                handleMsg(bsonInput,byteBuffer,packet);
                break;
            case 2004: // OP_QUERY
                handleQuery(bsonInput,packet);
                break;
            case 2001: // OP_UPDATE
                handleUpdate(bsonInput,packet);
                break;
            case 2002: // OP_INSERT
                handleInsert(bsonInput,byteBuffer,packet);
                break;
            case 2006: // OP_DELETE
                handleDelete(bsonInput,packet);
                break;

            // Add other cases for handling different opCodes

            default:
                System.err.println("Unknown opCode: " + opCode);
        }
    }

    private static void handleReply(ByteBufferBsonInput bsonInput, ByteBuf byteBuffer,MongoPacket packet) {
        try {
            System.out.println("======HANDLE REPLY");
            /*int messageLength = bsonInput.readInt32();
            int requestId = bsonInput.readInt32();
            int responseTo = bsonInput.readInt32();
            int opCode = bsonInput.readInt32();*/

            int responseFlags = bsonInput.readInt32();
            long cursorId = bsonInput.readInt64();
            int startingFrom = bsonInput.readInt32();
            int numberReturned = bsonInput.readInt32();

            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry());
            BsonDocumentCodec documentCodec = new BsonDocumentCodec(codecRegistry);
            BsonBinaryReader bsonReader = new BsonBinaryReader(bsonInput);

            System.out.println("ResponseFlags: " + responseFlags);
            System.out.println("CursorId: " + cursorId);
            System.out.println("StartingFrom: " + startingFrom);
            System.out.println("NumberReturned: " + numberReturned);

            for (int i = 0; i < numberReturned; i++) {
                BsonDocument document = documentCodec.decode(bsonReader, DecoderContext.builder().build());
                String json = document.toJson();
                System.out.println("Reply JSON: " + json);
            }
        } catch (Exception e) {
            System.err.println("Error decoding BSON reply message: " + e.getMessage());
        }
    }

    private static void handleInsert(ByteBufferBsonInput bsonInput, ByteBuf byteBuffer, MongoPacket packet) {
        try {

            System.out.println("======HANDLE INSERT");
            int flagBits = bsonInput.readInt32();
            packet.setFlagBits(flagBits);
            String fullCollectionName = bsonInput.readCString();
            packet.setFullCollectionName(fullCollectionName);

            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry());
            BsonDocumentCodec documentCodec = new BsonDocumentCodec(codecRegistry);
            BsonBinaryReader bsonReader = new BsonBinaryReader(bsonInput);

            MongoNamespace namespace = new MongoNamespace(fullCollectionName);

            System.out.println("Namespace: " + namespace);

            while (byteBuffer.hasRemaining()) {
                BsonDocument document = documentCodec.decode(bsonReader, DecoderContext.builder().build());
                String json = document.toJson();
                System.out.println("Insert JSON: " + json);
            }
        } catch (Exception e) {
            System.err.println("Error decoding BSON insert message: " + e.getMessage());
        }
    }

    private static void handleDelete(ByteBufferBsonInput bsonInput, MongoPacket packet) {
        try {
            System.out.println("======HANDLE DELETE");
            bsonInput.readInt32(); // skip ZERO
            String fullCollectionName = bsonInput.readCString();
            packet.setFullCollectionName(fullCollectionName);
            int flagBits = bsonInput.readInt32();
            packet.setFlagBits(flagBits);

            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry());
            BsonDocumentCodec documentCodec = new BsonDocumentCodec(codecRegistry);
            BsonBinaryReader bsonReader = new BsonBinaryReader(bsonInput);

            BsonDocument selector = documentCodec.decode(bsonReader, DecoderContext.builder().build());

            MongoNamespace namespace = new MongoNamespace(fullCollectionName);

            // Convert BSON document to JSON
            String selectorJson = selector.toJson();

            // Print out the JSON representation of the message
            System.out.println("Namespace: " + namespace);
            System.out.println("Delete JSON: " + selectorJson);
        } catch (Exception e) {
            System.err.println("Error decoding BSON delete message: " + e.getMessage());
        }
    }

    private static void handleUpdate(ByteBufferBsonInput bsonInput, MongoPacket packet) {
        try {
            System.out.println("======HANDLE UPDATE");
            bsonInput.readInt32(); // skip ZERO
            String fullCollectionName = bsonInput.readCString();
            packet.setFullCollectionName(fullCollectionName);
            int flagBits = bsonInput.readInt32();
            packet.setFlagBits(flagBits);

            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry());
            BsonDocumentCodec documentCodec = new BsonDocumentCodec(codecRegistry);
            BsonBinaryReader bsonReader = new BsonBinaryReader(bsonInput);

            BsonDocument selector = documentCodec.decode(bsonReader, DecoderContext.builder().build());
            BsonDocument update = documentCodec.decode(bsonReader, DecoderContext.builder().build());

            MongoNamespace namespace = new MongoNamespace(fullCollectionName);

            // Convert BSON documents to JSON
            String selectorJson = selector.toJson();
            String updateJson = update.toJson();

            // Print out the JSON representation of the message
            System.out.println("Namespace: " + namespace);
            System.out.println("Selector JSON: " + selectorJson);
            System.out.println("Update JSON: " + updateJson);
        } catch (Exception e) {
            System.err.println("Error decoding BSON update message: " + e.getMessage());
        }
    }

    private static void handleQuery(ByteBufferBsonInput bsonInput, MongoPacket packet) {
        try {
            System.out.println("======HANDLE QUERY");
            int flagBits = bsonInput.readInt32();
            packet.setFlagBits(flagBits);
            String fullCollectionName = bsonInput.readCString();
            packet.setFullCollectionName(fullCollectionName);
            int numberToSkip = bsonInput.readInt32();
            int numberToReturn = bsonInput.readInt32();

            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry());
            BsonDocumentCodec documentCodec = new BsonDocumentCodec(codecRegistry);
            BsonBinaryReader bsonReader = new BsonBinaryReader(bsonInput);
            BsonDocument query = documentCodec.decode(bsonReader, DecoderContext.builder().build());

            MongoNamespace namespace = new MongoNamespace(fullCollectionName);

            // Convert BSON document to JSON
            String json = query.toJson();

            // Print out the JSON representation of the message
            System.out.println("Namespace: " + namespace);
            System.out.println("Query JSON: " + json);
        } catch (Exception e) {
            System.err.println("Error decoding BSON query message: " + e.getMessage());
        }
    }

    private static void handleMsg(ByteBufferBsonInput bsonInput, ByteBuf byteBuffer, MongoPacket packet) {
        try {
            System.out.println("======HANDLE MESSAGE");

            int flagBits = bsonInput.readInt32();

            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry());
            BsonDocumentCodec documentCodec = new BsonDocumentCodec(codecRegistry);
            BsonBinaryReader bsonReader = new BsonBinaryReader(bsonInput);

            System.out.println("FlagBits: " + flagBits);

            while (byteBuffer.hasRemaining()) {
                int payloadType = bsonInput.readByte();
                //int payloadType2 = bsonInput.readByte();
                if(payloadType==0){
                    BsonDocument document = documentCodec.decode(bsonReader, DecoderContext.builder().build());
                    String json = document.toJson();
                    System.out.println("Document JSON: " + json);
                }else if(payloadType==1){
                    while (byteBuffer.hasRemaining()) {
                        try {
                        /*int length = bsonInput.readInt32();
                        String title = bsonInput.readCString();
                        BsonDocument document = documentCodec.decode(bsonReader, DecoderContext.builder().build());
                        String json = document.toJson();*/
                            BsonDocument document = documentCodec.decode(bsonReader, DecoderContext.builder().build());
                            String json = document.toJson();
                            System.out.println("Title JSON: " + json);
                            System.out.println("SubDocument JSON: " + json);
                        }catch (Exception ex){}
                    }
                }else{
                    bsonInput.readByte();
                    bsonInput.readByte();
                    bsonInput.readByte();
                }
                //String title = bsonInput.readCString();

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error decoding BSON message: " + e.getMessage());
        }
        System.out.println("ENDOFMSG");
    }

    // Implement this method to decompress the remaining bytes using the appropriate compressor (e.g., Snappy, Zlib, etc.)
    private static byte[] decompress(ByteBufferBsonInput bsonInput, byte compressorId, int compressedLength) throws IOException {
        var bb = new byte[compressedLength];
        bsonInput.readBytes(bb);
        if (compressorId == CompressorIds.noop) {
            return bb;
        }else if (compressorId == CompressorIds.zlib) {

            try {
                InflaterInputStream inflaterInputStream = new InflaterInputStream(new ByteArrayInputStream(bb));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inflaterInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                return outputStream.toByteArray();
            } catch (IOException e) {
                System.err.println("Error decompressing Zlib data: " + e.getMessage());
            }
        }else if (compressorId == CompressorIds.snappy) {
            return Snappy.uncompress(bb);
        } else if (compressorId == CompressorIds.zstd) {
            ZstdInputStream zstdInputStream = new ZstdInputStream(new ByteArrayInputStream(bb));
            ByteArrayOutputStream zstdOutputStream = new ByteArrayOutputStream();

            byte[] zstdBuffer = new byte[4096];
            int zstdBytesRead;
            while ((zstdBytesRead = zstdInputStream.read(zstdBuffer)) != -1) {
                zstdOutputStream.write(zstdBuffer, 0, zstdBytesRead);
            }

            return zstdOutputStream.toByteArray();
        } else {
            System.err.println("Unsupported");
        }
        return new byte[0];
    }
}