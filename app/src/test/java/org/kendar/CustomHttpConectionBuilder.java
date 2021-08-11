package org.kendar;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.junit.Assert;
import org.junit.Test;
import org.kendar.mocks.EnvironmentImpl;
import org.kendar.servers.AnsweringHttpServer;
import org.kendar.servers.dns.DnsMultiResolverImpl;
import org.kendar.servers.http.AnsweringHandlerImpl;
import org.kendar.servers.proxy.SimpleProxyHandlerImpl;
import org.kendar.utils.LoggerBuilderImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class CustomHttpConectionBuilder {
    @Test
    public void testDnsCustomResolver() throws IOException, InterruptedException {
        DnsResolver dnsResolver = new SystemDefaultDnsResolver() {
            @Override
            public InetAddress[] resolve(final String host) throws UnknownHostException {
                if (host.equalsIgnoreCase("my.host.com")) {
            /* If we match the host we're trying to talk to,
               return the IP address we want, not what is in DNS */
                    return new InetAddress[] { InetAddress.getByName("127.0.0.1") };
                } else {
                    /* Else, resolve it as we would normally */
                    return super.resolve(host);
                }
            }
        };
        BasicHttpClientConnectionManager connManager = new BasicHttpClientConnectionManager(
    /* We're forced to create a SocketFactory Registry.  Passing null
       doesn't force a default Registry, so we re-invent the wheel. */
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", SSLConnectionSocketFactory.getSocketFactory())
                        .build(),
                null, /* Default ConnectionFactory */
                null, /* Default SchemePortResolver */
                dnsResolver  /* Our DnsResolver */
        );

        var loggerBuilder = new LoggerBuilderImpl();
        var env = new EnvironmentImpl();
        var multiResolver = new DnsMultiResolverImpl(env,loggerBuilder);
        var simpleProxy = new SimpleProxyHandlerImpl(loggerBuilder,multiResolver,env);
        var answerer = new AnsweringHandlerImpl(loggerBuilder,multiResolver,null,simpleProxy);
        answerer.setHttpForwardProtocol("http");
        var server = new AnsweringHttpServer(loggerBuilder,answerer);
        server.setPort(20080);

        Thread thread = new Thread(server);
        thread.start();
        Thread.sleep(100);

        /* build HttpClient that will use our DnsResolver */
        HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connManager)
                .build();

        /* build our request */
        HttpGet httpRequest = new HttpGet("http://my.host.com:20080/page?and=stuff");

        httpRequest.addHeader(AnsweringHandlerImpl.MIRROR_REQUEST_HEADER,"true");
        /* Executing our request should now hit 127.0.0.1, regardless of DNS */
        HttpResponse httpResponse = httpClient.execute(httpRequest);
        HttpEntity responseEntity = httpResponse.getEntity();
        InputStream in = responseEntity.getContent();

        String body = IOUtils.toString(in, StandardCharsets.UTF_8);
        //System.out.println(body);
        //Assert.assertEquals(body,"{\"headers\":{\"Accept-encoding\":\"gzip,deflate\",\"Content-type\":\"application/x-www-form-urlencoded; charset=UTF-8\",\"Connection\":\"Keep-Alive\",\"Host\":\"localhost:20080\",\"Content-length\":\"25\",\"User-agent\":\"Apache-HttpClient/4.5.13 (Java/11.0.2)\"},\"query\":{},\"postParameters\":{\"foo\":\"bar\",\"employee\":\"John Doe\"},\"host\":\"localhost\",\"path\":\"/fuffa\",\"port\":20080,\"protocol\":\"http\",\"staticRequest\":false,\"binaryRequest\":false,\"soapRequest\":false,\"headerContentType\":\"application/x-www-form-urlencoded; charset=UTF-8\",\"headerSoapAction\":null,\"headerAuthorization\":null,\"basicUsername\":null,\"basicPassword\":null,\"method\":\"POST\",\"sanitizedPath\":\"localhost/fuffa\",\"multipart\":false,\"multipartData\":[],\"request\":null}");
        server.stop();
        Assert.assertTrue(body.contains("my.host.com:20080"));

    }
}
