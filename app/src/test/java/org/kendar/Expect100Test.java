package org.kendar;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.kendar.mocks.EnvironmentImpl;
import org.kendar.servers.AnsweringHttpServer;
import org.kendar.servers.dns.CustomHttpConectionBuilderImpl;
import org.kendar.servers.dns.DnsMultiResolverImpl;
import org.kendar.servers.http.AnsweringHandlerImpl;
import org.kendar.servers.proxy.SimpleProxyHandlerImpl;
import org.kendar.utils.LoggerBuilderImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.Security;

public class Expect100Test {

    @Test
    public void testExpect100() throws IOException {
        var loggerBuilder = new LoggerBuilderImpl();
        var env = new EnvironmentImpl();
        var multiResolver = new DnsMultiResolverImpl(env,loggerBuilder);
        var simpleProxy = new SimpleProxyHandlerImpl(loggerBuilder,multiResolver,env);
        var answerer = new AnsweringHandlerImpl(loggerBuilder,multiResolver,null,simpleProxy);
        var server = new AnsweringHttpServer(loggerBuilder,answerer);
        server.setPort(20080);

        Thread thread = new Thread(server);
        thread.start();


        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpRequest = new HttpGet("http://127.0.0.1:20080");

        httpRequest.addHeader(AnsweringHandlerImpl.TEST_EXPECT_100,"true");
        httpRequest.addHeader("Expect","100");
        /* Executing our request should now hit 127.0.0.1, regardless of DNS */
        HttpResponse httpResponse = httpClient.execute(httpRequest);
        HttpEntity responseEntity = httpResponse.getEntity();
        InputStream in = responseEntity.getContent();

        String body = IOUtils.toString(in, StandardCharsets.UTF_8);
        //System.out.println(body);
        //Assert.assertEquals(body,"{\"headers\":{\"Accept-encoding\":\"gzip,deflate\",\"Content-type\":\"application/x-www-form-urlencoded; charset=UTF-8\",\"Connection\":\"Keep-Alive\",\"Host\":\"localhost:20080\",\"Content-length\":\"25\",\"User-agent\":\"Apache-HttpClient/4.5.13 (Java/11.0.2)\"},\"query\":{},\"postParameters\":{\"foo\":\"bar\",\"employee\":\"John Doe\"},\"host\":\"localhost\",\"path\":\"/fuffa\",\"port\":20080,\"protocol\":\"http\",\"staticRequest\":false,\"binaryRequest\":false,\"soapRequest\":false,\"headerContentType\":\"application/x-www-form-urlencoded; charset=UTF-8\",\"headerSoapAction\":null,\"headerAuthorization\":null,\"basicUsername\":null,\"basicPassword\":null,\"method\":\"POST\",\"sanitizedPath\":\"localhost/fuffa\",\"multipart\":false,\"multipartData\":[],\"request\":null}");
        server.stop();
        Assert.assertTrue(body.contains("\"multipartData\":[]"));
    }


    @Test
    public void testExpect100continue() throws IOException {

        var loggerBuilder = new LoggerBuilderImpl();
        var env = new EnvironmentImpl();
        var multiResolver = new DnsMultiResolverImpl(env,loggerBuilder);
        var simpleProxy = new SimpleProxyHandlerImpl(loggerBuilder,multiResolver,env);
        var answerer = new AnsweringHandlerImpl(loggerBuilder,multiResolver,null,simpleProxy);
        var server = new AnsweringHttpServer(loggerBuilder,answerer);
        server.setPort(20080);

        Thread thread = new Thread(server);
        thread.start();


        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpRequest = new HttpGet("http://127.0.0.1:20080");

        httpRequest.addHeader(AnsweringHandlerImpl.TEST_EXPECT_100,"true");
        httpRequest.addHeader("Expect","100-continue");
        /* Executing our request should now hit 127.0.0.1, regardless of DNS */
        HttpResponse httpResponse = httpClient.execute(httpRequest);
        HttpEntity responseEntity = httpResponse.getEntity();
        InputStream in = responseEntity.getContent();

        String body = IOUtils.toString(in, StandardCharsets.UTF_8);
        //Assert.assertEquals(body,"{\"headers\":{\"Accept-encoding\":\"gzip,deflate\",\"Content-type\":\"application/x-www-form-urlencoded; charset=UTF-8\",\"Connection\":\"Keep-Alive\",\"Host\":\"localhost:20080\",\"Content-length\":\"25\",\"User-agent\":\"Apache-HttpClient/4.5.13 (Java/11.0.2)\"},\"query\":{},\"postParameters\":{\"foo\":\"bar\",\"employee\":\"John Doe\"},\"host\":\"localhost\",\"path\":\"/fuffa\",\"port\":20080,\"protocol\":\"http\",\"staticRequest\":false,\"binaryRequest\":false,\"soapRequest\":false,\"headerContentType\":\"application/x-www-form-urlencoded; charset=UTF-8\",\"headerSoapAction\":null,\"headerAuthorization\":null,\"basicUsername\":null,\"basicPassword\":null,\"method\":\"POST\",\"sanitizedPath\":\"localhost/fuffa\",\"multipart\":false,\"multipartData\":[],\"request\":null}");
        server.stop();
        Assert.assertTrue(body.contains("\"multipartData\":[]"));
    }


    @Test
    public void testExpect100continueEXTERNAL() throws IOException {

        try {
            var address = InetAddress.getByName("gorest.co.in");
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpRequest = new HttpGet("https://gorest.co.in/public/v1/users");
            httpRequest.addHeader("Expect", "100-continue");
            /* Executing our request should now hit 127.0.0.1, regardless of DNS */
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            HttpEntity responseEntity = httpResponse.getEntity();
            InputStream in = responseEntity.getContent();


            String body = IOUtils.toString(in, StandardCharsets.UTF_8);
            //System.out.println(body);

            for (Header header : httpResponse.getAllHeaders()) {
                //System.out.println(header.getName() + ":" + header.getValue());
            }
            Assert.assertTrue(body.contains("\"pagination\":"));
            Assert.assertEquals(200, httpResponse.getStatusLine().getStatusCode());
            //Assert.assertEquals(body,"{\"headers\":{\"Accept-encoding\":\"gzip,deflate\",\"Content-type\":\"application/x-www-form-urlencoded; charset=UTF-8\",\"Connection\":\"Keep-Alive\",\"Host\":\"localhost:20080\",\"Content-length\":\"25\",\"User-agent\":\"Apache-HttpClient/4.5.13 (Java/11.0.2)\"},\"query\":{},\"postParameters\":{\"foo\":\"bar\",\"employee\":\"John Doe\"},\"host\":\"localhost\",\"path\":\"/fuffa\",\"port\":20080,\"protocol\":\"http\",\"staticRequest\":false,\"binaryRequest\":false,\"soapRequest\":false,\"headerContentType\":\"application/x-www-form-urlencoded; charset=UTF-8\",\"headerSoapAction\":null,\"headerAuthorization\":null,\"basicUsername\":null,\"basicPassword\":null,\"method\":\"POST\",\"sanitizedPath\":\"localhost/fuffa\",\"multipart\":false,\"multipartData\":[],\"request\":null}");
        }catch(UnknownHostException ex){
            throw new AssumptionViolatedException("gorest unreachable");
        }
    }

    @Test
    public void testExpect100EXTERNAL() throws IOException {

        try {
            var loggerBuilder = new LoggerBuilderImpl();
            var env = new EnvironmentImpl();
            var multiResolver = new DnsMultiResolverImpl(env,loggerBuilder);

            var address = InetAddress.getByName("gorest.co.in");
            CloseableHttpClient httpClient = new CustomHttpConectionBuilderImpl(new LoggerBuilderImpl(),multiResolver).getConnection().build();
            HttpGet httpRequest = new HttpGet("https://gorest.co.in/public/v1/users");
            httpRequest.addHeader("Expect", "100");
            /* Executing our request should now hit 127.0.0.1, regardless of DNS */
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            HttpEntity responseEntity = httpResponse.getEntity();
            InputStream in = responseEntity.getContent();


            String body = IOUtils.toString(in, StandardCharsets.UTF_8);
            //System.out.println(body);

            for (Header header : httpResponse.getAllHeaders()) {
                //System.out.println(header.getName() + ":" + header.getValue());
            }
            Assert.assertTrue(body.contains("\"pagination\":"));
            Assert.assertEquals(200, httpResponse.getStatusLine().getStatusCode());
            //Assert.assertEquals(body,"{\"headers\":{\"Accept-encoding\":\"gzip,deflate\",\"Content-type\":\"application/x-www-form-urlencoded; charset=UTF-8\",\"Connection\":\"Keep-Alive\",\"Host\":\"localhost:20080\",\"Content-length\":\"25\",\"User-agent\":\"Apache-HttpClient/4.5.13 (Java/11.0.2)\"},\"query\":{},\"postParameters\":{\"foo\":\"bar\",\"employee\":\"John Doe\"},\"host\":\"localhost\",\"path\":\"/fuffa\",\"port\":20080,\"protocol\":\"http\",\"staticRequest\":false,\"binaryRequest\":false,\"soapRequest\":false,\"headerContentType\":\"application/x-www-form-urlencoded; charset=UTF-8\",\"headerSoapAction\":null,\"headerAuthorization\":null,\"basicUsername\":null,\"basicPassword\":null,\"method\":\"POST\",\"sanitizedPath\":\"localhost/fuffa\",\"multipart\":false,\"multipartData\":[],\"request\":null}");
        }catch(UnknownHostException ex){
            throw new AssumptionViolatedException("gorest unreachable");
        }
    }
}
