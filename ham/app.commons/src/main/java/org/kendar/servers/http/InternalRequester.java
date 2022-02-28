package org.kendar.servers.http;

public interface InternalRequester {
    void callSite(Request request, Response response)
            throws Exception;
}
