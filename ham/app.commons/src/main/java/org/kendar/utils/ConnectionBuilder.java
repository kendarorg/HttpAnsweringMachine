package org.kendar.utils;

import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;

public interface ConnectionBuilder {
    HttpClientConnectionManager getConnectionManger(boolean remoteDns);
    HttpClientConnectionManager getConnectionManger(boolean remoteDns,boolean checkSsl);
    CloseableHttpClient buildClient(boolean remoteDns, boolean checkSsl, int port,String protocol);
}
