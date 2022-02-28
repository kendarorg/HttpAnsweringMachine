package org.kendar.servers.http;

public interface ExternalRequester extends BaseRequester{
    void callSite(Request request, Response response)
            throws Exception;

}
