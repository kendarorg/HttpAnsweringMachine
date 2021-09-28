package org.kendar;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.Test;
import org.kendar.mocks.EnvironmentImpl;
import org.kendar.servers.AnsweringHttpServer;
import org.kendar.dns.DnsMultiResolverImpl;
import org.kendar.servers.http.AnsweringHandlerImpl;
import org.kendar.servers.proxy.SimpleProxyHandlerImpl;
import org.kendar.utils.LoggerBuilderImpl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ForwardRequestsTest {
    @Test
    public void testRequest() throws InterruptedException, IOException {
        var loggerBuilder = new LoggerBuilderImpl();
        var env = new EnvironmentImpl();
        var multiResolver = new DnsMultiResolverImpl(env,loggerBuilder);
        var simpleProxy = new SimpleProxyHandlerImpl(loggerBuilder,multiResolver,env);
        var answerer = new AnsweringHandlerImpl(loggerBuilder,multiResolver,null,simpleProxy,null);
        var server = new AnsweringHttpServer(loggerBuilder,answerer);
        server.setPort(20084);

        Thread thread = new Thread(server);
        thread.start();
        Thread.sleep(1000);

        HttpGet httpRequest = new HttpGet("http://localhost:20084");

        httpRequest.addHeader(AnsweringHandlerImpl.TEST_OVERWRITE_HOST,"https://www.microsoft.com");
        /* Executing our request should now hit 127.0.0.1, regardless of DNS */
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpResponse httpResponse = httpClient.execute(httpRequest);
        HttpEntity responseEntity = httpResponse.getEntity();
        InputStream in = responseEntity.getContent();

        String body = IOUtils.toString(in, StandardCharsets.UTF_8);
        server.stop();
        Assert.assertTrue(body.contains("twitter"));
        Assert.assertTrue(body.contains("Microsoft"));
    }

}
