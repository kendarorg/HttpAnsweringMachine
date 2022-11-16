package org.kendar.ham.fixes;

import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kendar.ham.*;
import org.kendar.utils.Sleeper;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HandlingOf5xxResponsesIT {
    private static HamBuilder hamBuilder = (HamBuilder)GlobalSettings.builder();
    private static String proxyId;

    @BeforeAll
    public static void beforeAll() throws HamTestException, HamException {
        HamStarter.runHamJar(HandlingOf5xxResponsesIT.class);
        proxyId = hamBuilder
                .proxies()
                .addProxy("http://www.local.test/testError","http://127.0.0.1:9091/testError","www.local.test:80");
    }

    @AfterAll
    public static void afterAll() throws HamException {
        hamBuilder.proxies().removeProxy(proxyId);

    }

    @ParameterizedTest
    @ValueSource(ints = {200,201})
    public void doTest2xx(int code) throws HamTestException, HamException {
        startWithCode(code,null,code);
    }

    @ParameterizedTest
    @ValueSource(ints = {304})
    public void doTest3xx(int code) throws HamTestException, HamException {
        startWithCode(code,null,code);
    }

    @ParameterizedTest
    @ValueSource(ints = {401,404})
    public void doTest4xx(int code) throws HamTestException, HamException {
        startWithCode(code,null,code);
    }

    @ParameterizedTest
    @ValueSource(ints = {500,501})
    public void doTest5xx(int code) throws HamTestException, HamException {
        startWithCode(code,null,code);
    }



    @ParameterizedTest
    @ValueSource(ints = {301,302})
    public void doTest3xxRedirect(int code) throws HamTestException, HamException {
        var hd = new HashMap<String,String>();
        hd.put("Location","https://www.facebook.com");
        startWithCode(code,hd,200);
    }

    private void startWithCode(int code, Map<String,String> headers,int expectedCode) throws HamTestException, HamException {
        var httpServer =  getHttpServer(9091, code,headers);
        try {
            System.out.println("Started server with "+ code);
            var httpGet = new HttpGet("http://www.local.test/testError");
            var clientResponse = hamBuilder.execute(httpGet);
            System.out.println("Retrieved response");
            assertEquals(expectedCode, clientResponse.getStatusLine().getStatusCode());
        }finally {
            httpServer.stop(0);
            Sleeper.sleep(200);
        }
    }


    private HttpServer getHttpServer(int gatewayPort,int throwEx,Map<String,String> headers) throws HamTestException {
        var server = LocalHttpServer.startServer(gatewayPort,
                new LocalHttpServer.LocalHandler("/testError", (call) -> {
                    try {
                        System.out.println("Responding with "+throwEx);
                        LocalHttpServer.sendResponse(call.httpExchange, throwEx, new byte[]{}, headers);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }));
        return server;
    }
}
