package org.kendar.ham;

import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kendar.utils.Sleeper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class HamReplayerIT {

    @BeforeAll
    public static void beforeAll() throws  HamTestException {
        HamStarter.runHamJar();
    }

    private HttpServer getHttpServer(int gatewayPort) throws HamTestException {
        var server = LocalHttpServer.startServer(gatewayPort,
                new LocalHttpServer.LocalHandler("/api/v2/$metadata", (call) -> {
                    try {
                        var httpGet2 = new HttpGet("https://www.nuget.org/api/v2/$metadata");
                        var clientResponse2 = hamBuilder.execute(httpGet2, true);
                        var data2 = IOUtils.toString(clientResponse2.getEntity().getContent(), StandardCharsets.UTF_8);
                        LocalHttpServer.sendResponse(call.httpExchange, 200, data2, null);
                    } catch (Exception e) {
                    }
                }));
        return server;
    }
    private HamBuilder hamBuilder = (HamBuilder)GlobalSettings.builder();

    @Test
    public void shouldBeAbleToUpload() throws IOException, HamException {
        var jsonContent = Files.readString(Path.of("test.json"));
        var request = hamBuilder.newRequest()
                .withPost()
                .withPath("/api/plugins/replayer/recording")
                .withHamFile("test.json",jsonContent,"application/json");
        hamBuilder.call(request.build());
    }
    @Test
    public void shouldBeAbleToConnectToServer() throws HamException, HamTestException, IOException {
        int gatewayPort = 9091;
        var dnsId =hamBuilder.dns().addDnsName("127.0.0.1", "gateway.int.test");
        var proxyId = hamBuilder.proxies().addProxy("http://gateway.int.test","http://127.0.0.1:"+gatewayPort,"127.0.0.1:80");

        HttpServer server = getHttpServer(gatewayPort);

        try {


            var httpGet = new HttpGet("http://gateway.int.test/api/v2/$metadata");
            var clientResponse = hamBuilder.execute(httpGet, true);
            var data = IOUtils.toString(clientResponse.getEntity().getContent(), StandardCharsets.UTF_8);
            System.out.println(data);
        }finally {
            hamBuilder.proxies().removeProxy(proxyId);
            hamBuilder.dns().removeDnsName(dnsId);
            server.stop(0);
        }
    }

}
