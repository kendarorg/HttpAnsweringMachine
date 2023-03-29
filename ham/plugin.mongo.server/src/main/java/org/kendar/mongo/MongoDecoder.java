package org.kendar.mongo;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class MongoDecoder {
    private final Socket socket;
    private final InputStream input;

    public MongoDecoder(Socket socket) throws IOException {
        this.socket = socket;
        this.input = socket.getInputStream();
    }

    public MongoPacket decode() throws IOException {
        // Read the length of the packet
        byte[] lengthBytes = new byte[4];
        readBytes(lengthBytes);
        int length = ByteBuffer.wrap(lengthBytes).getInt();

        // Read the request ID
        byte[] requestIdBytes = new byte[4];
        readBytes(requestIdBytes);
        int requestId = ByteBuffer.wrap(requestIdBytes).getInt();

        // Read the response ID
        byte[] responseIdBytes = new byte[4];
        readBytes(responseIdBytes);
        int responseId = ByteBuffer.wrap(responseIdBytes).getInt();

        // Read the op code
        byte[] opCodeBytes = new byte[4];
        readBytes(opCodeBytes);
        int opCode = ByteBuffer.wrap(opCodeBytes).getInt();

        // Read the payload
        byte[] payload = new byte[length - 16];
        readBytes(payload);

        // Construct the packet
        return new MongoPacket(requestId, responseId, opCode, payload);
    }

    private void readBytes(byte[] buffer) throws IOException {
        int offset = 0;
        int bytesRead = 0;
        while (offset < buffer.length && (bytesRead = input.read(buffer, offset, buffer.length - offset)) != -1) {
            offset += bytesRead;
        }
    }
}
