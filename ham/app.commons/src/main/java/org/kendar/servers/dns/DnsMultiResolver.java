package org.kendar.servers.dns;

import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.ThreeParamsFunction;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

public interface DnsMultiResolver {
    void noResponseCaching();

    void clearCache();

    List<String> resolve(String dnsName);
    List<String> resolveLocal(String dnsName);
    List<String> resolveRemote(String dnsName);
    HashMap<String,String> listDomains();

    void setRunnable(ThreeParamsFunction<String, String, LoggerBuilder, Callable<List<String>>> runnable);
}
