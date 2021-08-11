package org.kendar.servers.dns;

import org.apache.http.impl.client.HttpClientBuilder;

public interface CustomHttpConectionBuilder {
    HttpClientBuilder getConnection();
}
