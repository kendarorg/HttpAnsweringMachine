package org.kendar.servers.http;

public interface InternalRequester extends BaseRequester{
    void callSite(Request request, Response response)
            throws Exception;

}
