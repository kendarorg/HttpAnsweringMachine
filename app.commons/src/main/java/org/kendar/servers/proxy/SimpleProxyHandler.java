package org.kendar.servers.proxy;

import org.kendar.servers.http.Request;

import java.net.MalformedURLException;
import java.util.List;

public interface SimpleProxyHandler {
    Request translate(Request source) throws MalformedURLException;
    boolean ping(String host);
    List<RemoteServerStatus> getProxies();
}
