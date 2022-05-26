package org.kendar.ham.fixes;

import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.kendar.ham.HamStarter;
import org.kendar.ham.HamTestException;
import org.kendar.ham.LocalHttpServer;

import java.nio.charset.StandardCharsets;

public class HandlingOf5xxResponsesIT {
    public void doTest() throws HamTestException {
        HamStarter.runHamJar(HandlingOf5xxResponsesIT.class);
        var httpServer =  getHttpServer(9091,500);
    }


    private HttpServer getHttpServer(int gatewayPort,int throwEx) throws HamTestException {
        var server = LocalHttpServer.startServer(gatewayPort,
                new LocalHttpServer.LocalHandler("/api/call", (call) -> {
                    try {

                        LocalHttpServer.sendResponse(call.httpExchange, throwEx, new byte[]{}, null);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }));
        return server;
    }
}
