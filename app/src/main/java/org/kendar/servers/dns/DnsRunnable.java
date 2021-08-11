package org.kendar.servers.dns;

import org.xbill.DNS.*;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class DnsRunnable implements Callable<List<String>> {
    private String requestedServer;
    private String requestedDomain;

    public DnsRunnable(String requestedServer, String requestedDomain) {
        this.requestedServer = requestedServer;
        this.requestedDomain = requestedDomain;
    }

    @Override
    public List<String> call() throws Exception {
        SimpleResolver resolver = null;
        List<String> result = new ArrayList<>();
        try {
            resolver = new SimpleResolver(requestedServer);
            resolver.setPort(53);
            var resolvers = new ArrayList<Resolver>();
            resolvers.add(resolver);
            ExtendedResolver extendedResolver = new ExtendedResolver(resolvers);
            extendedResolver.setTimeout(Duration.ofSeconds(1));
            extendedResolver.setRetries(2);
            Lookup lookup = new Lookup(requestedDomain, Type.A);
            lookup.setResolver(extendedResolver);
            lookup.setCache(null);
            lookup.setHostsFileParser(null);
            var records = lookup.run();
            if(records!=null){
                for (Record record:records) {
                    String realip = ((ARecord) records[0]).getAddress().getHostAddress();

                    result.add(realip);

                }
            }
        } catch (UnknownHostException | TextParseException e) {
            return new ArrayList<>();
        }
        return result;
    }
}
