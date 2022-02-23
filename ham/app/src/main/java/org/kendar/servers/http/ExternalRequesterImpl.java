package org.kendar.servers.http;

import com.networknt.schema.format.InetAddressValidator;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.utils.LoggerBuilder;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Component
public class ExternalRequesterImpl extends BaseRequesterImpl implements ExternalRequester{

    public ExternalRequesterImpl(RequestResponseBuilder requestResponseBuilder, DnsMultiResolver multiResolver, LoggerBuilder loggerBuilder) {
        super(requestResponseBuilder, multiResolver, loggerBuilder);
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
    public SystemDefaultDnsResolver buildResolver() {
        return  new SystemDefaultDnsResolver() {
            @Override
            public InetAddress[] resolve(final String host) throws UnknownHostException {
                AnsweringHandlerImpl.ResolvedDomain descriptor;
                var currentTime = Calendar.getInstance().getTimeInMillis();
                List<String> hosts;
                if (domains.containsKey(host)) {
                    descriptor = domains.get(host);
                    if ((descriptor.timestamp + 10 * 60 * 1000) < currentTime) {
                        domains.remove(host);
                        hosts = multiResolver.resolveRemote(host);
                        descriptor = new AnsweringHandlerImpl.ResolvedDomain();
                        descriptor.domains.addAll(hosts);
                        domains.put(host, descriptor);
                    }
                } else {
                    hosts = multiResolver.resolveRemote(host);
                    descriptor = new AnsweringHandlerImpl.ResolvedDomain();
                    descriptor.domains.addAll(hosts);
                    domains.put(host, descriptor);
                }
                hosts = new ArrayList<>(descriptor.domains);
                var address = new InetAddress[hosts.size()];
                for (int i = 0; i < hosts.size(); i++) {
                    address[i] = InetAddress.getByName(hosts.get(i));
                }
                return address;
            }
        };
    }
}
