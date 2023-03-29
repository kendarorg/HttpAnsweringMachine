package org.kendar.mongo;

import org.apache.commons.lang3.SystemUtils;
import org.bson.BsonBinaryReader;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.Decoder;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.json.JsonWriterSettings;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

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

    public void readBytes(byte[] buffer) throws IOException {
        int offset = 0;
        int bytesRead = 0;
        while (offset < buffer.length && (bytesRead = input.read(buffer, offset, buffer.length - offset)) != -1) {
            offset += bytesRead;
        }
    }

    //https://gist.github.com/rozza/9c94808ed5b4f1edca75
    @Override
    public void run() {
        try {
            while (true) {
                //Read from client
                MongoPacket mongoPacket = getMongoPacket();
                //Send to server
                //Read from server
                //Send back to client
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private MongoPacket getMongoPacket() throws IOException {
        byte[] lengthBytes = new byte[4];
        readBytes(lengthBytes);
        int length = ByteBuffer.wrap(lengthBytes).getInt();

        var bb = ByteBuffer.allocateDirect(length);
        bb.put(lengthBytes);

        // Read the request ID
        byte[] requestIdBytes = new byte[4];
        readBytes(requestIdBytes);
        int requestId = ByteBuffer.wrap(requestIdBytes).getInt();
        bb.put(requestIdBytes, 4, 4);

        // Read the response ID
        byte[] responseIdBytes = new byte[4];
        readBytes(responseIdBytes);
        int responseId = ByteBuffer.wrap(responseIdBytes).getInt();
        bb.put(responseIdBytes, 8, 4);

        // Read the op code
        byte[] opCodeBytes = new byte[4];
        readBytes(opCodeBytes);
        int opCode = ByteBuffer.wrap(opCodeBytes).getInt();
        bb.put(opCodeBytes, 12, 4);

        // Read the payload
        byte[] payload = new byte[length - 16];
        readBytes(payload);
        bb.put(payload, 16, payload.length);

        byte[] fullMessage = new byte[length];
        bb.position(0);
        bb.get(fullMessage,0,length);
        bb.position(0);

        BsonBinaryReader bsonReader = new BsonBinaryReader(bb);
        Document document = DOCUMENT_CODEC.decode(bsonReader, DecoderContext.builder().build());
        var mongoPacket = new MongoPacket(requestId, responseId, opCode, fullMessage, document);
        return mongoPacket;
    }
}