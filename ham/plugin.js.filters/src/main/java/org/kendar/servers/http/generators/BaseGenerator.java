package org.kendar.servers.http.generators;

import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;

public interface BaseGenerator {
    String getType();
    String getDescription();
    String generate(Request request, Response response);
}
