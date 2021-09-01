package org.kendar.servers;

import org.kendar.dns.DnsServer;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xbill.DNS.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AnsweringDnsServer implements AnsweringServer {
    public void isSystem(){};

    private boolean running =false;
    private final Logger logger;
    private DnsServer dnsServer;
    @Value( "${dns.enabled:true}" )
    private boolean enabled;

    private ConcurrentHashMap<String,String> missingRecords = new ConcurrentHashMap<>();

    public AnsweringDnsServer(LoggerBuilder loggerBuilder, DnsServer dnsServer){
        this.logger = loggerBuilder.build(AnsweringDnsServer.class);
        this.dnsServer = dnsServer;

    }



    @Override
    public void run() {
        if(running)return;
        if(!enabled)return;
        running=true;

        try {
            dnsServer.run();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            running=false;
        }

    }



    @Override
    public boolean shouldRun() {
        return enabled && !running;
    }
}
