package org.kendar.mongo;

import com.github.luben.zstd.ZstdInputStream;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoNamespace;
import org.bson.BsonBinaryReader;
import org.bson.BsonDocument;
import org.bson.ByteBuf;
import org.bson.ByteBufNIO;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.io.ByteBufferBsonInput;
import org.kendar.mongo.model.*;
import org.xerial.snappy.Snappy;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.InflaterInputStream;

public class MongoClientHandler implements Runnable {

    private final Socket client;

    public MongoClientHandler(Socket client) {
        this.client = client;
    }

    public static void readBytes(InputStream stream,byte[] buffer) throws IOException {
        int offset = 0;
        int bytesRead = 0;
        while (offset < buffer.length && (bytesRead = stream.read(buffer, offset, buffer.length - offset)) != -1) {
            offset += bytesRead;
        }
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
                    System.out.println("===================");
                    System.out.println("==FROM CLIENT");
                    var clientPacket = readPacketsFromStream(fromClient, headerBytes);
                    toMongoDb.write(clientPacket.getHeader());
                    toMongoDb.write(clientPacket.getPayload());
                    toMongoDb.flush();
                    readBytes(fromMongoDb,mongoHeaderBytes);
                    System.out.println("==FROM SERVER");
                    var mongoPacket = readPacketsFromStream(fromMongoDb, mongoHeaderBytes);
                    if(mongoPacket.isFinale()){
                        break;
                    }
                    toClient.write(mongoPacket.getHeader());
                    toClient.write(mongoPacket.getPayload());
                    toClient.flush();
                    System.out.println("===================");

                    headerBytes = new byte[16];
                    mongoHeaderBytes = new byte[16];
                }
            }
        }
    }

    private static MongoPacket readPacketsFromStream(InputStream inputStream, byte[] headerBytes) throws IOException {
        ByteBufferBsonInput headerInput = new ByteBufferBsonInput(
                ByteBufNIOcreate(headerBytes, ByteOrder.LITTLE_ENDIAN));
        int messageLength = headerInput.readInt32();
        int requestId = headerInput.readInt32();
        int responseTo = headerInput.readInt32();
        int opCode = headerInput.readInt32();
        var packet = new MongoPacket();
        if(messageLength==0 && opCode==0 && requestId==0 && responseTo==0){
            packet.setHeader(headerBytes);
            packet.setPayload(new byte[]{});
            return packet;
        }

        byte[] remainingBytes = new byte[messageLength - 16];

        readBytes(inputStream,remainingBytes);
        //inputStream.read(remainingBytes);
        packet.setHeader(headerBytes);
        packet.setPayload(remainingBytes);
        System.out.println("=>"+opCode+"  "+remainingBytes.length);

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

                var cpacket = new CompressedMongoPacket();
                cpacket.setHeader(buildHeader(uncompressedSize + 16, requestId, responseTo, originalOpCode));
                cpacket.setPayload(decompressedBytes);
                packet.setMessage(cpacket);
                var byteBuffer = ByteBufNIOcreate(decompressedBytes, ByteOrder.LITTLE_ENDIAN);
                ByteBufferBsonInput decompressedInput = new ByteBufferBsonInput(byteBuffer);

                handleOpCode(originalOpCode, decompressedInput, byteBuffer, cpacket,decompressedBytes.length);
            } else {
                handleOpCode(opCode, remainingInput, uncompressedByteBuffer, packet,remainingBytes.length);
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

    private static void handleOpCode(int opCode, ByteBufferBsonInput bsonInput, ByteBuf byteBuffer, MongoPacket packet,
                                     int length) {
        switch (opCode) {
            case 1: // OP_REPLY
                handleReply(bsonInput,packet);
                break;
            case 2013: // OP_MSG
                handleMsg(bsonInput,byteBuffer,packet,length);
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

    private static void handleReply(ByteBufferBsonInput bsonInput,MongoPacket packet) {
        try {
            System.out.println("======HANDLE REPLY");

            int responseFlags = bsonInput.readInt32();
            long cursorId = bsonInput.readInt64();
            int startingFrom = bsonInput.readInt32();
            int numberReturned = bsonInput.readInt32();
            var replyPacket = new ReplyPacket();
            replyPacket.setResponseFlags(responseFlags);
            replyPacket.setCursorId(cursorId);
            replyPacket.setStartingFrom(startingFrom);
            replyPacket.setNumberReturned(numberReturned);
            packet.setMessage(replyPacket);


            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry());


            System.out.println("ResponseFlags: " + responseFlags);
            System.out.println("CursorId: " + cursorId);
            System.out.println("StartingFrom: " + startingFrom);
            System.out.println("NumberReturned: " + numberReturned);

            for (int i = 0; i < numberReturned; i++) {
                BsonDocumentCodec documentCodec = new BsonDocumentCodec(codecRegistry);
                BsonBinaryReader bsonReader = new BsonBinaryReader(bsonInput);
                BsonDocument document = documentCodec.decode(bsonReader, DecoderContext.builder().build());
                String json = document.toJson();
                replyPacket.getJsons().add(json);
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
            String fullCollectionName = bsonInput.readCString();

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
            int flagBits = bsonInput.readInt32();

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
            int flagBits = bsonInput.readInt32();

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
            String fullCollectionName = bsonInput.readCString();
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

    private static void handleMsg(ByteBufferBsonInput bsonInput, ByteBuf byteBuffer, MongoPacket packet, int length) {
        try {
            System.out.println("======HANDLE MESSAGE");

            var subPacket = new MsgPacket();
            packet.setMessage(subPacket);
            int flagBits = bsonInput.readInt32();
            subPacket.setFlagBits(flagBits);

            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry());



            System.out.println("FlagBits: " + flagBits);

            while (byteBuffer.position()<length) {
                var remaining = length - byteBuffer.position();
                if(remaining==4){
                    subPacket.setChecksum(bsonInput.readInt32());
                    //Only checksum
                    break;
                }else {
                    int payloadType = bsonInput.readByte();
                    //int payloadType2 = bsonInput.readByte();
                    if (payloadType == 0) {
                        BsonDocumentCodec documentCodec = new BsonDocumentCodec(codecRegistry);
                        BsonBinaryReader bsonReader = new BsonBinaryReader(bsonInput);
                        BsonDocument document = documentCodec.decode(bsonReader, DecoderContext.builder().build());
                        String json = document.toJson();
                        var pl = new MsgDocumentPayload();
                        pl.setJson(json);
                        subPacket.getPayloads().add(pl);
                        System.out.println("Document JSON: " + json);
                    } else if (payloadType == 1) {
                        var pl = new MsgSectionPayload();
                        subPacket.getPayloads().add(pl);

                        pl.setLength(bsonInput.readInt32());

                        var end = byteBuffer.position()+pl.getLength() - 4;
                        pl.setTitle(bsonInput.readCString());
                        System.out.println(pl.getTitle()+":"+pl.getLength());

                        while(byteBuffer.position()<end) {
                            System.out.println("READING DOC");
                            BsonBinaryReader bsonReader = new BsonBinaryReader(bsonInput);
                            BsonDocumentCodec documentCodec = new BsonDocumentCodec(codecRegistry);
                            BsonDocument document = documentCodec.decode(bsonReader, DecoderContext.builder().build());
                            String json = document.toJson();
                            var doc = new MsgDocumentPayload();
                            doc.setJson(json);
                            pl.getDocuments().add(doc);
                        }
                    }
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
        if (compressorId == CompressorIds.NOOP) {
            return bb;
        }else if (compressorId == CompressorIds.ZLIB) {

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
        }else if (compressorId == CompressorIds.SNAPPY) {
            return Snappy.uncompress(bb);
        } else if (compressorId == CompressorIds.Z_STD) {
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