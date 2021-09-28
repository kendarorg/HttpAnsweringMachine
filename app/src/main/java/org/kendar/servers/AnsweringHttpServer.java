package org.kendar.servers;

import com.sun.net.httpserver.HttpServer;
import org.kendar.servers.http.AnsweringHandler;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

@Component
public class AnsweringHttpServer implements AnsweringServer {
    public void isSystem(){};
    private final Logger logger;
    private final AnsweringHandler handler;
    private boolean running =false;
    @Value( "${http.enabled:true}" )
    private final boolean enabled =true;
    @Value( "${http.backlog:50}" )
    private int backlog;
    @Value( "${http.port:80}" )
    private int port;
    @Value( "${http.useCachedExecutor:true}" )
    private final boolean useCachedExecutor = false;
    private HttpServer httpServer;


    public AnsweringHttpServer(LoggerBuilder loggerBuilder, AnsweringHandler handler){
        this.logger = loggerBuilder.build(AnsweringHttpsServer.class);
        this.handler = handler;
    }

    @Override
    public void run(){
        if(running)return;
        if(!enabled)return;
        running=true;

        try {
            // setup the socket address
            InetSocketAddress address = new InetSocketAddress(port);

            // initialise the HTTPS server
            httpServer = HttpServer.create(address, backlog);

            httpServer.createContext("/", handler);
            if(useCachedExecutor) {
                httpServer.setExecutor(Executors.newCachedThreadPool());    // creates a cached
            }else {
                httpServer.setExecutor(null);   // creates a default executor
            }

            httpServer.start();

            logger.info("Http server LOADED, port: "+port);
            while(running){
                Thread.sleep(10000);
            }
        } catch (Exception ex) {
            logger.error("Failed to create HTTP server on port " + port + " of localhost",ex);
        }finally {
            running=false;
        }
    }

    @Override
    public boolean shouldRun() {
        return enabled && !running;
    }

    public void setPort(int port){
        this.port = port;
    }

    public void stop(){
        httpServer.stop(1);
        running = false;

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
