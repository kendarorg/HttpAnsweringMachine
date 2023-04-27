package org.kendar.ham.fixes;

import com.sun.net.httpserver.HttpServer;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kendar.ham.GlobalSettings;
import org.kendar.ham.HamBuilder;
import org.kendar.ham.HamException;
import org.tkendar.ham.HamStarter;
import org.tkendar.ham.HamTestException;
import org.tkendar.ham.LocalHttpServer;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class HandlingOf5xxResponsesIT {
    private static final HamBuilder hamBuilder = (HamBuilder) GlobalSettings.builder();
    private static String proxyId;
    private static HttpServer server;

    @BeforeAll
    public static void beforeAll() throws HamTestException, HamException {
        server = getHttpServer(9091);
        HamStarter.runHamJar(HandlingOf5xxResponsesIT.class);
        proxyId = hamBuilder
                .proxies()
                .addProxy("http://www.local.test/testError", "http://127.0.0.1:9091/testError", "www.local.test:80");
    }

    @AfterAll
    public static void afterAll() throws HamException {
        hamBuilder.proxies().removeProxy(proxyId);
        server.stop(0);

    }

    private static Map<String, String> queryToMap(String query) {
        if (query == null) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    private static HttpServer getHttpServer(int gatewayPort) throws HamTestException {
        var server = LocalHttpServer.startServer(gatewayPort,
                new LocalHttpServer.LocalHandler("/testError", (call) -> {
                    try {

                        System.out.println("Requested data");
                        var headers = new HashMap<String, String>();
                        var code = Integer.valueOf(call.query.get("code"));
                        if (call.query.containsKey("location")) {
                            headers.put("Location", call.query.get("location"));
                        }
                        System.out.println("Responding with " + code);
                        LocalHttpServer.sendResponse(call.httpExchange, code, new byte[]{}, headers);

                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }));
        return server;
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 201, 401, 404, 500, 501, 304})
    public void doTestA(int code) throws HamTestException, HamException {
        System.out.println("Proposed " + code);
        var httpGet = new HttpGet("http://www.local.test/testError?code=" + code);
        var clientResponse = hamBuilder.execute(httpGet);
        assertEquals(code, clientResponse.getStatusLine().getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(ints = {301, 302})
    public void doTestB(int code) throws HamTestException, HamException {
        System.out.println("Proposed " + code);
        var hd = new HashMap<String, String>();
        hd.put("Location", "https://www.facebook.com");
        var httpGet = new HttpGet("http://www.local.test/testError?code=" + code +
                "&location=http://www.local.test");
        var clientResponse = hamBuilder.execute(httpGet);
        assertNotEquals(code, clientResponse.getStatusLine().getStatusCode());
    }
}
