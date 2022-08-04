package org.kendar.freemaker.ham;

public class ReqResponse {
    public String id;
    public int status;
    public String path;
    public String contentType;
    public byte[] request;
    public byte[] response;
    public String method;
    public String host;
    public boolean isTextResponse;
}
