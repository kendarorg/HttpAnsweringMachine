package org.kendar.servers.http;

import com.networknt.schema.format.InetAddressValidator;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.utils.ConnectionBuilder;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

@Component
public class ExternalRequesterImpl extends BaseRequesterImpl implements ExternalRequester{

    public ExternalRequesterImpl(RequestResponseBuilder requestResponseBuilder, DnsMultiResolver multiResolver,
                                 LoggerBuilder loggerBuilder, ConnectionBuilder connectionBuilder) {
        super(requestResponseBuilder, multiResolver, loggerBuilder,connectionBuilder);
    }

    @Override
    public void callSite(Request request, Response response)
            throws Exception {
        var resolved = multiResolver.resolveRemote(request.getHost());
        if(resolved.size()==0){
            if(!InetAddressValidator.getInstance().isValidInet4Address(request.getHost())) {
                response.setStatusCode(404);
                return;
            }
        }
        super.callSite(request,response);
    }

    @Override
    protected boolean useRemoteDnsOnly() {
        return true;
    }
}
