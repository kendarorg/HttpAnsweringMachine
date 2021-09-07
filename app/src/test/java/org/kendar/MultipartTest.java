package org.kendar;

import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Test;
import org.kendar.mocks.EnvironmentImpl;
import org.kendar.servers.AnsweringHttpServer;
import org.kendar.dns.DnsMultiResolverImpl;
import org.kendar.servers.http.AnsweringHandlerImpl;
import org.kendar.servers.proxy.SimpleProxyHandlerImpl;
import org.kendar.utils.LoggerBuilderImpl;
import org.kendar.utils.SimpleStringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MultipartTest {
    @Test
    public void testSplit(){

        var result = SimpleStringUtils.splitByString("Boundary:"," asfasd boundAry: giapeto");
        Assert.assertEquals(result[0]," asfasd ");
        Assert.assertEquals(result[1]," giapeto");
    }

    @Test
    public void testFormUrlEncoded() throws IOException, InterruptedException {
        var loggerBuilder = new LoggerBuilderImpl();
        var env = new EnvironmentImpl();
        var multiResolver = new DnsMultiResolverImpl(env,loggerBuilder);
        var simpleProxy = new SimpleProxyHandlerImpl(loggerBuilder,multiResolver,env);
        var answerer = new AnsweringHandlerImpl(loggerBuilder,multiResolver,null,simpleProxy);
        var server = new AnsweringHttpServer(loggerBuilder,answerer);
        server.setPort(20080);

        Thread thread = new Thread(server);
        thread.start();
        Thread.sleep(100);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        List<NameValuePair> form = new ArrayList<>();
        form.add(new BasicNameValuePair("foo", "bar"));
        form.add(new BasicNameValuePair("employee", "John Doe"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);

        HttpPost httpPost = new HttpPost("http://localhost:20080/fuffa");
        httpPost.setEntity(entity);
        httpPost.addHeader(AnsweringHandlerImpl.MIRROR_REQUEST_HEADER,"true");
        CloseableHttpResponse response = httpClient.execute(httpPost);
        HttpEntity responseEntity = response.getEntity();
        InputStream in = responseEntity.getContent();

        String body = IOUtils.toString(in, StandardCharsets.UTF_8);
        //System.out.println(body);
        //Assert.assertEquals(body,"{\"headers\":{\"Accept-encoding\":\"gzip,deflate\",\"Content-type\":\"application/x-www-form-urlencoded; charset=UTF-8\",\"Connection\":\"Keep-Alive\",\"Host\":\"localhost:20080\",\"Content-length\":\"25\",\"User-agent\":\"Apache-HttpClient/4.5.13 (Java/11.0.2)\"},\"query\":{},\"postParameters\":{\"foo\":\"bar\",\"employee\":\"John Doe\"},\"host\":\"localhost\",\"path\":\"/fuffa\",\"port\":20080,\"protocol\":\"http\",\"staticRequest\":false,\"binaryRequest\":false,\"soapRequest\":false,\"headerContentType\":\"application/x-www-form-urlencoded; charset=UTF-8\",\"headerSoapAction\":null,\"headerAuthorization\":null,\"basicUsername\":null,\"basicPassword\":null,\"method\":\"POST\",\"sanitizedPath\":\"localhost/fuffa\",\"multipart\":false,\"multipartData\":[],\"request\":null}");
        server.stop();
        Assert.assertTrue(body.contains("\"foo\":\"bar\""));
        Assert.assertTrue(body.contains("\"employee\":\"John Doe\""));
        Assert.assertTrue(body.contains("headerContentType\":\"application/x-www-form-urlencoded; charset=UTF-8"));

    }

    @Test
    public void testMultipart() throws IOException {
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
        HttpPost uploadFile = new HttpPost("http://localhost:20080/fuffa");
        uploadFile.addHeader(AnsweringHandlerImpl.MIRROR_REQUEST_HEADER,"true");
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        //builder.addTextBody("field1", "yes", ContentType.TEXT_PLAIN);

// This attaches the file to the POST:;
        var bytes = "testContent".getBytes();
        builder.addBinaryBody("file1",bytes,ContentType.APPLICATION_OCTET_STREAM, "test.file");

        HttpEntity multipart = builder.build();
        uploadFile.setEntity(multipart);
        CloseableHttpResponse response = httpClient.execute(uploadFile);
        HttpEntity responseEntity = response.getEntity();
        InputStream in = responseEntity.getContent();

        String body = IOUtils.toString(in, StandardCharsets.UTF_8);
        //Assert.assertEquals(body,"{\"headers\":{\"Accept-encoding\":\"gzip,deflate\",\"Content-type\":\"multipart/form-data; boundary=1LFaYQF7_3Iy6s6XiZhhjHMERNfBSu8O5pp5d_g\",\"Connection\":\"Keep-Alive\",\"Host\":\"localhost:20080\",\"Content-length\":\"246\",\"User-agent\":\"Apache-HttpClient/4.5.13 (Java/11.0.2)\"},\"query\":{},\"postParameters\":{},\"host\":\"localhost\",\"path\":\"/fuffa\",\"port\":20080,\"protocol\":\"http\",\"staticRequest\":false,\"binaryRequest\":true,\"soapRequest\":false,\"headerContentType\":\"multipart/form-data; boundary=1LFaYQF7_3Iy6s6XiZhhjHMERNfBSu8O5pp5d_g\",\"headerSoapAction\":null,\"headerAuthorization\":null,\"basicUsername\":null,\"basicPassword\":null,\"method\":\"POST\",\"sanitizedPath\":\"localhost/fuffa\",\"multipart\":true,\"multipartData\":[{\"headers\":{\"Content-Disposition\":\" form-data; name=\\\"file1\\\"; filename=\\\"test.file\\\"\",\"Content-Transfer-Encoding\":\" binary\",\"Content-Type\":\" application/octet-stream\"},\"data\":\"testContent\"}],\"request\":null}");
        //System.out.println(body);
        server.stop();
        Assert.assertTrue(body.contains("\"headerContentType\":\"multipart/form-data; boundary="));
        Assert.assertTrue(body.contains("filename=\\\"test.file\\"));
        Assert.assertTrue(body.contains("\"data\":\"testContent\""));
    }
}
