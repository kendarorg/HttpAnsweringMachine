package org.kendar.mongo.compressor;

public interface CompressionHandler {
    byte[] decompress(byte[] bb);

    int getId();
}
