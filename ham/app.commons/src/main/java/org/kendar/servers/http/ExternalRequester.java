package org.kendar.servers.http;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public interface ExternalRequester {
    void callExternalSite(Request request, Response response)
            throws Exception;
    PoolingHttpClientConnectionManager getConnectionManager();
}
