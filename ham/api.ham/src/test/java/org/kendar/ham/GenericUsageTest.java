package org.kendar.ham;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tkendar.ham.HamStarter;
import org.tkendar.ham.HamTestException;
import org.tkendar.ham.LocalHttpServer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GenericUsageTest {

    private static String proxyHttp;
    private static String proxyHttps;
    ObjectMapper smileMapper = new ObjectMapper(new SmileFactory());
    public static final String LOCAL_STARTING_ADDRESS = "http://www.local.test/gut";
    public static final String HTTPS_LOCAL_STARTING_ADDRESS = "https://www.local.test/gut";

    private static Map<String, Object> parseQuery(String query)
            throws UnsupportedEncodingException {

        Map<String, Object> parameters = new HashMap<>();
        if (query != null) {
            String pairs[] = query.split("[&]");

            for (String pair : pairs) {
                String param[] = pair.split("[=]");

                String key = null;
                String value = null;
                if (param.length > 0) {
                    key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
                }

                if (param.length > 1) {
                    value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
                }

                if (parameters.containsKey(key)) {
                    Object obj = parameters.get(key);
                    if (obj instanceof List<?>) {
                        List<String> values = (List<String>) obj;
                        values.add(value);
                    } else if (obj instanceof String) {
                        List<String> values = new ArrayList<String>();
                        values.add((String) obj);
                        values.add(value);
                        parameters.put(key, values);
                    }
                } else {
                    parameters.put(key, value);
                }
            }
        }
        return parameters;
    }
    private static HttpServer server;
    private static final HamBuilder hamBuilder = (HamBuilder)GlobalSettings.builder();
    //private static HamBuilder hamBuilderHttpProxy = (HamBuilder)GlobalSettings.builderHttpProxy();

    @BeforeAll
    static void beforeAll() throws HamTestException, HamException {
         server = getHttpServer(5983);
        HamStarter.runHamJar(DnsTest.class);
        proxyHttp = hamBuilder
                .proxies()
                .addProxy(LOCAL_STARTING_ADDRESS, "http://127.0.0.1:5983", "127.0.0.1:80");
        proxyHttps = hamBuilder
                .proxies()
                .addProxy(HTTPS_LOCAL_STARTING_ADDRESS, "http://127.0.0.1:5983", "127.0.0.1:80");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {

        }

    }
    @AfterAll
    static void afterAll() throws HamException {
        hamBuilder
                .proxies().removeProxy(proxyHttp);
        hamBuilder
                .proxies().removeProxy(proxyHttps);
        server.stop(0);
    }
    private static final ObjectMapper mapper = new ObjectMapper();
    private static HttpServer getHttpServer(int gatewayPort) throws HamTestException {
        var server = LocalHttpServer.startServer(gatewayPort,
                handleStandardBodyPost(),
                handleStandardBodyPostSmile(),
                handlePostParameters());
        return server;
    }

    private static LocalHttpServer.LocalHandler handlePostParameters() {
        return new LocalHttpServer.LocalHandler("/request/post/params", (call) -> {
            try {
                var headers = new HashMap<String, String>();
                headers.put("content-type", "text/plain");
                var data = call.getStringData();
                var parsed = parseQuery(data);
                LocalHttpServer.sendResponse(call.httpExchange, 200,
                        mapper.writeValueAsString(parsed), headers);
            } catch (Exception e) {
                try {
                    LocalHttpServer.sendResponse(call.httpExchange, 500, e.getMessage(), null);
                } catch (HamTestException ex) {
                    System.err.println(ex);
                }
            }
        });
    }
    private static LocalHttpServer.LocalHandler handleStandardBodyPost() {
        return new LocalHttpServer.LocalHandler("/request/post/body", (call) -> {
            try {
                var headers = new HashMap<String, String>();
                headers.put("content-type", "text/plain");
                var data = call.getStringData();
                LocalHttpServer.sendResponse(call.httpExchange, 200, data, headers);
            } catch (Exception e) {
                try {
                    LocalHttpServer.sendResponse(call.httpExchange, 500, e.getMessage(), null);
                } catch (HamTestException ex) {
                    System.err.println(ex);
                }
            }
        });
    }

    private static LocalHttpServer.LocalHandler handleStandardBodyPostSmile() {
        return new LocalHttpServer.LocalHandler("/request/post/smile", (call) -> {
            try {
                var headers = new HashMap<String, String>();
                headers.put("content-type", "application/octet-stream");
                LocalHttpServer.sendResponse(call.httpExchange, 200, call.data, headers);
            } catch (Exception e) {
                try {
                    LocalHttpServer.sendResponse(call.httpExchange, 500, e.getMessage(), null);
                } catch (HamTestException ex) {
                    System.err.println(ex);
                }
            }
        });
    }

    @Test
    public void testPostBody() throws HamException, IOException {
        var httpPost = new HttpPost(LOCAL_STARTING_ADDRESS+"/request/post/body");
        httpPost.setEntity(new StringEntity("testString"));
        var clientResponse = hamBuilder.execute(httpPost, true);
        var resultData = IOUtils.toString(clientResponse.getEntity().getContent(), StandardCharsets.UTF_8);
        assertEquals("testString",resultData);
    }

    @Test
    public void testPostBodyHttps() throws HamException, IOException {
        var httpPost = new HttpPost(HTTPS_LOCAL_STARTING_ADDRESS+"/request/post/body");
        httpPost.setEntity(new StringEntity("testString"));
        var clientResponse = hamBuilder.execute(httpPost, true);
        var resultData = IOUtils.toString(clientResponse.getEntity().getContent(), StandardCharsets.UTF_8);
        assertEquals("testString",resultData);
    }

    //@Test
//    public void testPostBodyHttpsProxyHttp() throws HamException, IOException {
//        var httpPost = new HttpPost(LOCAL_STARTING_ADDRESS+"/request/post/body");
//        httpPost.setEntity(new StringEntity("testString"));
//        var clientResponse = hamBuilderHttpProxy.execute(httpPost, true);
//        var resultData = IOUtils.toString(clientResponse.getEntity().getContent(), StandardCharsets.UTF_8);
//        assertEquals("testString",resultData);
//    }

    @Test
    public void testPostParameters() throws HamException, IOException {
        var httpPost = new HttpPost(LOCAL_STARTING_ADDRESS +"/request/post/params");

        var params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", "@username"));
        params.add(new BasicNameValuePair("password", "@password"));
        httpPost.setEntity(new UrlEncodedFormEntity(params));

        var clientResponse = hamBuilder.execute(httpPost, true);
        var resultData = IOUtils.toString(clientResponse.getEntity().getContent(), StandardCharsets.UTF_8);
        assertTrue(resultData.contains("\"password\":\"@password\""));
        assertTrue(resultData.contains("\"username\":\"@username\""));
    }

    @Test
    public void testPostBodySmile() throws HamException, IOException {
        var httpPost = new HttpPost(LOCAL_STARTING_ADDRESS+"/request/post/smile");
        var map = new HashMap<String,String>();
        map.put("a","b");
        var smileMap = smileMapper.writeValueAsBytes(map);
        httpPost.addHeader("content-type","application/x-jackson-smile");
        httpPost.setEntity(new ByteArrayEntity(smileMap));
        var clientResponse = hamBuilder.execute(httpPost, true);
        var resultData = IOUtils.toByteArray(clientResponse.getEntity().getContent());
        var jsonNodes = smileMapper.readValue(resultData, JsonNode.class);
        var resultDataString = mapper.writeValueAsString(jsonNodes);
        assertTrue(resultDataString.contains("\"a\":\"b\""));
    }
}
