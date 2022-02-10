package org.kendar.servers.dns;

import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.ThreeParamsFunction;

import java.util.List;
import java.util.concurrent.Callable;

public interface DnsMultiResolver {
    List<String> resolve(String dnsName);
    List<String> resolveLocal(String dnsName);
    List<String> resolveRemote(String dnsName);

    void setRunnable(ThreeParamsFunction<String, String, LoggerBuilder, Callable<List<String>>> runnable);
}
