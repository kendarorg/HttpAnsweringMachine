package org.kendar.dns;

public class BlockedLoop {
    public BlockedLoop(long timestamp){

        this.count = 1;
        this.timestamp = timestamp;
    }
    public int count;
    public long timestamp;
}
