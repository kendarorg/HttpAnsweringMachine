package org.kendar;

public class CustomHttpConectionBuilder {
    /*
    @Test
    public void testDnsCustomResolver() throws IOException, InterruptedException {
        DnsResolver dnsResolver = new SystemDefaultDnsResolver() {
            @Override
            public InetAddress[] resolve(final String host) throws UnknownHostException {
                if (host.equalsIgnoreCase("my.host.com")) {
            // If we match the host we're trying to talk to,
            //   return the IP address we want, not what is in DNS 
                    return new InetAddress[] { InetAddress.getByName("127.0.0.1") };
                } else {
                    // Else, resolve it as we would normally
                    return super.resolve(host);
                }
            }
        };
        BasicHttpClientConnectionManager connManager = new BasicHttpClientConnectionManager(
    // We're forced to create a SocketFactory Registry.  Passing null
     //  doesn't force a default Registry, so we re-invent the wheel. 
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", SSLConnectionSocketFactory.getSocketFactory())
                        .build(),
                null, // Default ConnectionFactory 
                null, // Default SchemePortResolver 
                dnsResolver  // Our DnsResolver 
        );

        var loggerBuilder = new LoggerBuilderImpl();
        var env = new EnvironmentImpl();
        var multiResolver = new DnsMultiResolverImpl(env,loggerBuilder);
        var simpleProxy = new SimpleProxyHandlerImpl(loggerBuilder,multiResolver,env);
        var answerer = new AnsweringHandlerImpl(loggerBuilder,multiResolver,null,simpleProxy,null);
        answerer.setHttpForwardProtocol("http");
        var server = new AnsweringHttpServer(loggerBuilder,answerer);
        server.setPort(20080);

        Thread thread = new Thread(server);
        thread.start();
        Thread.sleep(100);

        // build HttpClient that will use our DnsResolver 
        HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connManager)
                .build();

        // build our request 
        HttpGet httpRequest = new HttpGet("http://my.host.com:20080/page?and=stuff");

        httpRequest.addHeader(AnsweringHandlerImpl.MIRROR_REQUEST_HEADER,"true");
        // Executing our request should now hit 127.0.0.1, regardless of DNS 
        HttpResponse httpResponse = httpClient.execute(httpRequest);
        HttpEntity responseEntity = httpResponse.getEntity();
        InputStream in = responseEntity.getContent();

        String body = IOUtils.toString(in, StandardCharsets.UTF_8);
        server.stop();
        Assert.assertTrue(body.contains("my.host.com:20080"));

    }*/
}
