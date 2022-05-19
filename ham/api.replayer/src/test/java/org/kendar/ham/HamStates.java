package org.kendar.ham;

import com.sun.net.httpserver.HttpServer;
import io.cucumber.java.en.Given;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HamStates  extends BaseStates{


    @Given("^I have a running HAM instance$")
    public void iHaveRunningHamInstance() throws HamTestException {
        HamStarter.runHamJar();
    }

    @Given("^I add a dns mapping from '(.+)' to '(.+)'$")
    public void iAddDnsMapping(String ip,String name) throws HamTestException, HamException {
        var dnsId =hamBuilder.dns().addDnsName(ip,name);
        hamBuilder.certificates().addAltName(name);
        dnses.add(dnsId);
    }

    @Given("^I add a proxy from '(.+)' to '(.+)' testing it with '(.+)'$")
    public void iAddAProxy(String from,String to,String test) throws HamTestException, HamException {
        var proxyId = hamBuilder.proxies().addProxy(from,to,test);
        proxies.add(proxyId);
    }

    @Given("^I have a server listening on port '([0-9]+)'$")
    public void iHaveServerListemimgOn(int port) throws HamTestException {
        if(httpServer!=null)return;
        httpServer =  getHttpServer(port);
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
                        System.out.println(e.getMessage());
                    }
                }));
        return server;
    }

    @Given("^user calls '(.+)'$")
    public void user_calls_url(String url) throws HamException, IOException {
        /*var urlReal = new URL(url);
        var request = hamBuilder.newRequest()
                .withProtocol(urlReal.getProtocol())
                .withHost(urlReal.getHost())
                .withPath(urlReal.getPath());
        var result = hamBuilder.call(request.build());
        resultData = result.getResponseText();*/
        var httpGet = new HttpGet(url);
        var clientResponse = hamBuilder.execute(httpGet, true);
        resultData = IOUtils.toString(clientResponse.getEntity().getContent(), StandardCharsets.UTF_8);


    }

    @Given("^the response should contain '(.*)'$")
    public void the_response_should_be_(String data) {
        if(data==null && resultData==null) return;
        if(data!=null && data.isEmpty() && resultData==null) return;
        assertTrue(resultData.contains(data));
    }
}
