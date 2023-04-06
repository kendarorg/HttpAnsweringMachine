package org.kendar.mongo.compressor;

import org.springframework.stereotype.Component;

@Component
public class NoopCompressionHandler implements CompressionHandler{
    @Override
    public byte[] decompress(byte[] bb) {
        return bb;
    }

    @Override
    public int getId() {
        return CompressorIds.NOOP;
    }
}
