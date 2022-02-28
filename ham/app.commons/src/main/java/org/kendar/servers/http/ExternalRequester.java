package org.kendar.servers.http;

public interface ExternalRequester {
    void callSite(Request request, Response response)
            throws Exception;
}
