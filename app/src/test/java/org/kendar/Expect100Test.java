package org.kendar;

public class Expect100Test {
/*
    @Test
    public void testExpect100() throws IOException {
        var loggerBuilder = new LoggerBuilderImpl();
        var env = new EnvironmentImpl();
        var multiResolver = new DnsMultiResolverImpl(env,loggerBuilder);
        var simpleProxy = new SimpleProxyHandlerImpl(loggerBuilder,multiResolver,env);
        var answerer = new AnsweringHandlerImpl(loggerBuilder,multiResolver,null,simpleProxy,null);
        var server = new AnsweringHttpServer(loggerBuilder,answerer);
        server.setPort(20080);

        Thread thread = new Thread(server);
        thread.start();


        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpRequest = new HttpGet("http://127.0.0.1:20080");

        httpRequest.addHeader(AnsweringHandlerImpl.TEST_EXPECT_100,"true");
        httpRequest.addHeader("Expect","100");
        // Executing our request should now hit 127.0.0.1, regardless of DNS 
        HttpResponse httpResponse = httpClient.execute(httpRequest);
        HttpEntity responseEntity = httpResponse.getEntity();
        InputStream in = responseEntity.getContent();

        String body = IOUtils.toString(in, StandardCharsets.UTF_8);
        server.stop();
        Assert.assertTrue(body.contains("\"multipartData\":[]"));
    }


    @Test
    public void testExpect100continue() throws IOException {

        var loggerBuilder = new LoggerBuilderImpl();
        var env = new EnvironmentImpl();
        var multiResolver = new DnsMultiResolverImpl(env,loggerBuilder);
        var simpleProxy = new SimpleProxyHandlerImpl(loggerBuilder,multiResolver,env);
        var answerer = new AnsweringHandlerImpl(loggerBuilder,multiResolver,null,simpleProxy,null);
        var server = new AnsweringHttpServer(loggerBuilder,answerer);
        server.setPort(20080);

        Thread thread = new Thread(server);
        thread.start();


        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpRequest = new HttpGet("http://127.0.0.1:20080");

        httpRequest.addHeader(AnsweringHandlerImpl.TEST_EXPECT_100,"true");
        httpRequest.addHeader("Expect","100-continue");
        // Executing our request should now hit 127.0.0.1, regardless of DNS 
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
            // Executing our request should now hit 127.0.0.1, regardless of DNS 
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
            CloseableHttpClient httpClient = new CustomHttpConectionBuilderImpl(new LoggerBuilderImpl(),multiResolver,null).getConnection().build();
            HttpGet httpRequest = new HttpGet("https://gorest.co.in/public/v1/users");
            httpRequest.addHeader("Expect", "100");
            // Executing our request should now hit 127.0.0.1, regardless of DNS 
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
    }*/
}
