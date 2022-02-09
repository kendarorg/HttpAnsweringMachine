package org.kendar.dns;

import org.kendar.dns.configurations.DnsConfig;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.ThreeParamsFunction;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.xbill.DNS.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
public class DnsServer {
  private static final int UDP_SIZE = 512;

  private final int dnsPort;
  private final Logger logger;
  private final DnsMultiResolver multiResolver;
  private final ConcurrentHashMap<String, Integer> loopBlocker = new ConcurrentHashMap<>();
  private Function<String, String> blocker;


  public DnsServer(
      LoggerBuilder loggerBuilder,
      DnsMultiResolver multiResolver,
      JsonConfiguration configuration) {

    this.blocker = (a)->a.toUpperCase(Locale.ROOT);
    this.logger = loggerBuilder.build(DnsServer.class);
    this.multiResolver = multiResolver;
    this.dnsPort = configuration.getConfiguration(DnsConfig.class).getPort();
  }

  public void setDnsRunnable(ThreeParamsFunction<String,String,LoggerBuilder, Callable<List<String>>> runnable){
    this.multiResolver.setRunnable(runnable);
  }

  @SuppressWarnings("InfiniteLoopStatement")
  public void run() throws IOException {
    DatagramSocket socket = new DatagramSocket(dnsPort);
    // socket.setSoTimeout(0);
    logger.info("Dns server LOADED, port: " + dnsPort);
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
      resolveAll(indp, socket, in);
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

  private void resolveAll(DatagramPacket indp, DatagramSocket socket, byte[] in) {
    try {
      // Build the response
      Message request;
      request = new Message(in);
      Message response = new Message(request.getHeader().getID());
      String requestedDomain = request.getQuestion().getName().toString(true);
      logger.debug("Requested domain " + requestedDomain);

      List<String> ips = new ArrayList<>();

      response.addRecord(request.getQuestion(), Section.QUESTION);

        if (!requestedDomain.equals(blocker.apply(requestedDomain))) {
          ips = this.multiResolver.resolve(blocker.apply(requestedDomain));
        }

      byte[] resp;
      if (ips.size() > 0) {
        for (String ip : ips) {
          logger.debug("FOUNDED IP " + ip + " FOR " + requestedDomain);
          // Add answers as needed
          response.addRecord(
              Record.fromString(Name.root, Type.A, DClass.IN, 86400, ip, Name.root),
              Section.ANSWER);
        }
        resp = response.toWire();
      } else {
        resp = errorMessage(request, Rcode.NXDOMAIN);
      }
      logger.debug("SENDING RESPONSE");
      DatagramPacket outdp =
          new DatagramPacket(resp, resp.length, indp.getAddress(), indp.getPort());
      socket.send(outdp);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void setBlocker(Function<String,String> blocker) {
    this.blocker = blocker;
  }
}
