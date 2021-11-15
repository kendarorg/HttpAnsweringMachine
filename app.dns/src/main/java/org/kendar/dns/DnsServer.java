package org.kendar.dns;

import org.kendar.dns.configurations.DnsConfig;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.xbill.DNS.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DnsServer {
    private static final int UDP_SIZE = 512;

    private int dnsPort;
    private final Logger logger;
    private final DnsMultiResolver multiResolver;

    public DnsServer(LoggerBuilder loggerBuilder, DnsMultiResolver multiResolver, JsonConfiguration configuration){

        this.logger = loggerBuilder.build(DnsServer.class);
        this.multiResolver = multiResolver;
        this.dnsPort = configuration.getConfiguration(DnsConfig.class).getPort();
    }

    public void run() throws IOException {
        DatagramSocket socket = new DatagramSocket(dnsPort);
        //socket.setSoTimeout(0);
        logger.info("Dns server LOADED, port: "+dnsPort);
        byte[] in = new byte[UDP_SIZE];

        // Read the request
        DatagramPacket indp = new DatagramPacket(in, UDP_SIZE);
        while (true) {
            indp.setLength(in.length);

            try {
                socket.receive(indp);
            } catch (InterruptedIOException e) {
                continue;
            }
            resolveAll(indp,socket, in);
        }
    }

    byte[] buildErrorMessage(Header header, int rcode, Record question) {
        Message response = new Message();
        response.setHeader(header);
        for (int i = 0; i < 4; i++) {
            response.removeAllRecords(i);
        }
        if (rcode == Rcode.SERVFAIL) {
            response.addRecord(question, Section.QUESTION);
        }
        header.setRcode(rcode);
        return response.toWire();
    }

    public byte[] formerrMessage(byte[] in) {
        Header header;
        try {
            header = new Header(in);
        } catch (IOException e) {
            return null;
        }
        return buildErrorMessage(header, Rcode.FORMERR, null);
    }

    public byte[] errorMessage(Message query, int rcode) {
        return buildErrorMessage(query.getHeader(), rcode, query.getQuestion());
    }

    private ConcurrentHashMap<String,Integer> loopBlocker = new ConcurrentHashMap<>();

    private void resolveAll(DatagramPacket indp,DatagramSocket socket, byte[] in) {
        try {
            // Build the response
            Message request = null;
            request = new Message(in);
            Message response = new Message(request.getHeader().getID());
            String requestedDomain = request.getQuestion().getName().toString(true);
            logger.debug("Requested domain "+requestedDomain);

            List<String> ips = new ArrayList<>();


            response.addRecord(request.getQuestion(), Section.QUESTION);
            var fromLocalHost = indp.getAddress().toString().contains("127.0.0.1");

            if(fromLocalHost) {
                if(!requestedDomain.equals(requestedDomain.toUpperCase(Locale.ROOT))){
                    ips = this.multiResolver.resolve(requestedDomain.toUpperCase(Locale.ROOT), fromLocalHost);
                }
            }else{
                ips = this.multiResolver.resolve(requestedDomain, fromLocalHost);
            }

            byte[] resp = new byte[0];
            if(ips.size()>0) {
                for (String ip : ips) {
                    logger.debug("FOUNDED IP " + ip + " FOR " + requestedDomain);
                    // Add answers as needed
                    response.addRecord(Record.fromString(Name.root, Type.A, DClass.IN, 86400, ip, Name.root), Section.ANSWER);
                }
                resp = response.toWire();
            }else{
                resp = errorMessage(request, Rcode.NXDOMAIN);
            }
            logger.debug("SENDING RESPONSE");
            DatagramPacket outdp = new DatagramPacket(resp, resp.length, indp.getAddress(), indp.getPort());
            socket.send(outdp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
