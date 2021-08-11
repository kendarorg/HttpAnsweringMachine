package org.kendar.http;

import org.apache.http.conn.HttpClientConnectionManager;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;

import java.lang.reflect.InvocationTargetException;

public interface FilteringClassesHandler {
    boolean handle(HttpFilterType filterType, Request request, Response response, HttpClientConnectionManager connectionManager) throws InvocationTargetException, IllegalAccessException;
}
