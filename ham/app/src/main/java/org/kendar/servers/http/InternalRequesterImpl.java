package org.kendar.servers.http;

import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class InternalRequesterImpl extends BaseRequesterImpl implements InternalRequester{

    public InternalRequesterImpl(RequestResponseBuilder requestResponseBuilder, DnsMultiResolver multiResolver, LoggerBuilder loggerBuilder) {
        super(requestResponseBuilder, multiResolver, loggerBuilder);
    }

    @Override
    public SystemDefaultDnsResolver buildResolver() {
        return  new SystemDefaultDnsResolver() {
            @Override
            public InetAddress[] resolve(final String host) throws UnknownHostException {
                var result = multiResolver.resolve(host);
                if(result.size()>1) return new InetAddress[]{InetAddress.getByName(result.get(0))};
                return new InetAddress[]{};
            }
        };
    }
}
