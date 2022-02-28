package org.kendar.servers.http;

public interface BaseRequester {
    void callSite(Request request, Response response)
            throws Exception;
}
