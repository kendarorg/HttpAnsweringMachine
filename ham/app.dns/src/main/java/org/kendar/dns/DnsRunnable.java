package org.kendar.dns;

import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.xbill.DNS.*;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class DnsRunnable implements Callable<List<String>> {
    private final String requestedServer;
    private final String requestedDomain;
    private final Logger logger;

    public DnsRunnable(String requestedServer, String requestedDomain, LoggerBuilder loggerBuilder) {
        this.requestedServer = requestedServer;
        this.requestedDomain = requestedDomain;
        this.logger = loggerBuilder.build(DnsRunnable.class);
    }

    @Override
    public List<String> call() throws Exception {
        SimpleResolver resolver;
        List<String> result = new ArrayList<>();
        try {
            logger.debug("Request to "+requestedServer+" for "+requestedDomain);
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
                for (org.xbill.DNS.Record record:records) {
                    String realip = ((ARecord) records[0]).getAddress().getHostAddress();
                    logger.debug("Resolved with "+requestedServer+" for "+requestedDomain+ ": "+realip);
                    result.add(realip);

                }
            }
        } catch (UnknownHostException | TextParseException e) {
            throw new UnknownHostException();
        }
        if(result==null || result.size()==0){
            logger.debug("NOTRESOLVED with "+requestedServer+" for "+requestedDomain);
        }
        return result;
    }
}
