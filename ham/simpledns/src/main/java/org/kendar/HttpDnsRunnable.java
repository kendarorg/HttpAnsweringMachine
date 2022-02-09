package org.kendar;

import org.kendar.dns.DnsDirectCaller;
import org.kendar.dns.DnsRunnable;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class HttpDnsRunnable implements Callable<List<String>> {
    private final String requestedServer;
    private final String requestedDomain;
    private final LoggerBuilder loggerBuilder;

    public HttpDnsRunnable(String requestedServer, String requestedDomain, LoggerBuilder loggerBuilder) {
        this.requestedServer = requestedServer;
        this.requestedDomain = requestedDomain;
        this.loggerBuilder = loggerBuilder;
    }

    @Override
    public List<String> call() throws Exception {
        var caller = new DnsDirectCaller(loggerBuilder);
        var result = caller.testDnsServer(requestedServer,requestedDomain);
        if(result == null || result.isEmpty()) return new ArrayList<>();
        var rs = new ArrayList<String>();
        rs.add(result);
        return rs;
    }
}
