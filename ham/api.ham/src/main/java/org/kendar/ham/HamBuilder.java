package org.kendar.ham;

public class HamBuilder {

    public HamBuilder(String host, int port){
        this(host,port,port==443);
    }

    public HamBuilder(String host, int port,boolean https){
    }
}
