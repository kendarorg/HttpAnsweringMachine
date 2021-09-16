package org.kendar.dns;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xbill.DNS.*;

@Component
public class DnsServer {
    private static final int UDP_SIZE = 512;

    @Value( "${dns.port:53}" )
    private int dnsPort;
    private Logger logger;
    private DnsMultiResolver multiResolver;

    public DnsServer(LoggerBuilder loggerBuilder, DnsMultiResolver multiResolver){

        this.logger = loggerBuilder.build(DnsServer.class);
        this.multiResolver = multiResolver;
    }

    public void run() throws IOException {
        multiResolver.verify();
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

    private void resolveAll(DatagramPacket indp,DatagramSocket socket, byte[] in) {
        try {
            // Build the response
            Message request = null;
            request = new Message(in);
            Message response = new Message(request.getHeader().getID());
            String requestedDomain = request.getQuestion().getName().toString(true);
            logger.debug("Requested domain "+requestedDomain);
            response.addRecord(request.getQuestion(), Section.QUESTION);

            var ips = this.multiResolver.resolve(requestedDomain);
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
            //response = new Message(request.getHeader().getID());
            /*var header = new Header();
            header.setID(request.getHeader().getID());
            //header.setOpcode(Opcode.QUERY);
            header.setRcode(Rcode.NXDOMAIN);
            response.setHeader(header);*/

            //DatagramSocket socket = new DatagramSocket();
            logger.debug("SENDING RESPONSE");
            DatagramPacket outdp = new DatagramPacket(resp, resp.length, indp.getAddress(), indp.getPort());
            socket.send(outdp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
