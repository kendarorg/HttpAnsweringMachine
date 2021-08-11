package org.kendar.servers;

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
    private static final int MAX_DNS_THREADS = 10;
    private static final int UDP_SIZE = 512;

    private boolean running =false;
    private final Logger logger;
    private DnsMultiResolver multiResolver;
    @Value( "${dns.port:53}" )
    private int dnsPort;
    @Value( "${dns.enabled:true}" )
    private boolean enabled;

    private ConcurrentHashMap<String,String> missingRecords = new ConcurrentHashMap<>();

    public AnsweringDnsServer(LoggerBuilder loggerBuilder, DnsMultiResolver multiResolver){
        this.logger = loggerBuilder.build(AnsweringDnsServer.class);
        this.multiResolver = multiResolver;

    }



    @Override
    public void run() {
        if(running)return;
        if(!enabled)return;
        running=true;

        try {
            DatagramSocket socket = new DatagramSocket(dnsPort);
            socket.setSoTimeout(0);
            logger.info("Dns server LOADED, port: "+dnsPort);
            while (true) {
                process(socket);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            running=false;
        }

    }

    private void resolveAll(DatagramPacket indp, DatagramSocket socket, byte[] in) {
        try {
            // Build the response
            Message request = null;
            request = new Message(in);
            Message response = new Message(request.getHeader().getID());
            String requestedDomain = request.getQuestion().getName().toString(true);
            logger.debug("Requested domain "+requestedDomain);
            response.addRecord(request.getQuestion(), Section.QUESTION);

            var ips = this.multiResolver.resolve(requestedDomain);

            if(ips.size()>0) {
                for (String ip : ips) {
                    logger.debug("FOUNDED IP " + ip + " FOR " + requestedDomain);
                    // Add answers as needed
                    response.addRecord(Record.fromString(Name.root, Type.A, DClass.IN, 86400, ip, Name.root), Section.ANSWER);

                }
            }

            byte[] resp = response.toWire();
            //DatagramSocket socket = new DatagramSocket();
            logger.debug("SENDING RESPONSE");
            DatagramPacket outdp = new DatagramPacket(resp, resp.length, indp.getAddress(), indp.getPort());
            socket.send(outdp);
        } catch (NullPointerException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void process(DatagramSocket socket) throws IOException {
        byte[] in = new byte[UDP_SIZE];

        // Read the request
        DatagramPacket indp = new DatagramPacket(in, UDP_SIZE);
        socket.receive(indp);

        //executor.execute(responder);
        resolveAll(indp,socket,in);
    }



    @Override
    public boolean shouldRun() {
        return enabled && !running;
    }
}
