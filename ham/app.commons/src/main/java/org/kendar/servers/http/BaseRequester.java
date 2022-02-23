package org.kendar.servers.http;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public interface BaseRequester {
    void callSite(Request request, Response response)
            throws Exception;
    PoolingHttpClientConnectionManager getConnectionManager();
}
