package org.kendar.mongo.compressor;

import org.springframework.stereotype.Component;
import org.xerial.snappy.Snappy;

import java.io.IOException;

@Component
public class SnappyCompressionHandler implements CompressionHandler {
    @Override
    public byte[] decompress(byte[] bb) {
        try {
            return Snappy.uncompress(bb);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getId() {
        return CompressorIds.SNAPPY;
    }
}
