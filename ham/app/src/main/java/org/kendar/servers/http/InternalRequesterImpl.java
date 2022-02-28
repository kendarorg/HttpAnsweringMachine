package org.kendar.servers.http;

import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.utils.ConnectionBuilder;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

@Component
public class InternalRequesterImpl extends BaseRequesterImpl implements InternalRequester{

    public InternalRequesterImpl(RequestResponseBuilder requestResponseBuilder, DnsMultiResolver multiResolver,
                                 LoggerBuilder loggerBuilder, ConnectionBuilder connnectionBuilder) {
        super(requestResponseBuilder, multiResolver, loggerBuilder,connnectionBuilder);
    }

    @Override
    protected boolean useRemoteDnsOnly() {
        return false;
    }
}
