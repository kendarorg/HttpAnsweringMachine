package org.kendar.servers.dns;

import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.PropertiesManager;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class CustomHttpConectionBuilderImpl implements CustomHttpConectionBuilder{

    private final Logger logger;
    private final DnsMultiResolver dnsMultiResolver;
    private PropertiesManager propertiesManager;

    private HttpClientConnectionManager connManager;
    private SystemDefaultDnsResolver dnsResolver;


    public CustomHttpConectionBuilderImpl(LoggerBuilder loggerBuilder, DnsMultiResolver  dnsMultiResolver,
                                          PropertiesManager propertiesManager){
        this.logger = loggerBuilder.build(CustomHttpConectionBuilderImpl.class);
        this.dnsMultiResolver = dnsMultiResolver;
        this.propertiesManager = propertiesManager;
    }


    @PostConstruct
    public void init(){
        this.dnsResolver = new SystemDefaultDnsResolver() {
            @Override
            public InetAddress[] resolve(final String host) throws UnknownHostException {
                var hosts = dnsMultiResolver.resolve(host,false);
                var address = new InetAddress[hosts.size()];
                for(int i=0;i< hosts.size();i++){
                    address[i] = InetAddress.getByName(hosts.get(i));
                }
                if (address.length>0) {
                    /* If we match the host we're trying to talk to,
                       return the IP address we want, not what is in DNS */
                    return address;
                } else {
                    /* Else, resolve it as we would normally */
                    return super.resolve(host);
                }
            }
        };
        this.connManager = new PoolingHttpClientConnectionManager(
                // We're forced to create a SocketFactory Registry.  Passing null
                //   doesn't force a default Registry, so we re-invent the wheel.
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", SSLConnectionSocketFactory.getSocketFactory())
                        .build(),
                dnsResolver  // Our DnsResolver
        );
    }

    @Override
    public HttpClientBuilder getConnection(){
        return HttpClientBuilder.create()
                .setConnectionManager(connManager);
    }
}
