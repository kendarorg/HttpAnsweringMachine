package org.kendar.ham;

public class HamBuilder {
    private final String host;
    private final int port;
    private final boolean https;

    public HamBuilder(String host, int port){
        this(host,port,port==443);
    }

    public HamBuilder(String host, int port,boolean https){
        this.host = host;
        this.port = port;
        this.https = https;
    }
}
