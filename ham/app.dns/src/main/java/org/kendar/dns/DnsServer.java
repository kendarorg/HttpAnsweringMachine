package org.kendar.dns;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

import org.kendar.dns.configurations.DnsConfig;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.GlobalConfig;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.ThreeParamsFunction;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.xbill.DNS.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
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
    String localAddress = configuration.getConfiguration(GlobalConfig.class).getLocalAddress();
  }

  public void setDnsRunnable(ThreeParamsFunction<String,String,LoggerBuilder, Callable<List<String>>> runnable){
    this.multiResolver.setRunnable(runnable);
  }

  private void runTcp(){
    try {
      ServerSocket serverSocket = new ServerSocket(dnsPort);



      while (running.get()) {
        Socket clientSocket = serverSocket.accept();
        executorService.submit(()->{

          try{
            var in = clientSocket.getInputStream();
            DataInputStream dis = new DataInputStream(in);

            int len = dis.readUnsignedShort();
            byte[] data = new byte[len];
            if (len > 0) {
              dis.readFully(data);
            }
            resolveAll(clientSocket.getOutputStream(),data);
            clientSocket.close();

          } catch (IOException e) {
            logger.error("ERror reading from DNS tcp stream",e);
          }
        });
      }
    }catch (Exception ex){
      logger.error("Error running tcp thread",ex);
    }
  }

  private void resolveAll(OutputStream outputStream, byte[] in) {
    try {
      byte[] resp = buildResponse(in);
      var dataOutputStream= new DataOutputStream(outputStream);
      short length = (short) resp.length;
      ByteBuffer buffer = ByteBuffer.allocate(2);
      buffer.putShort(length);
      dataOutputStream.write(buffer.array());
      dataOutputStream.write(resp);
      dataOutputStream.flush();
      logger.debug("SENDING RESPONSE");
    }catch(Exception ex){
      ex.printStackTrace();
    }
  }

  private final ExecutorService executorService = Executors.newFixedThreadPool(20);


  private final AtomicBoolean running = new AtomicBoolean(false);

  private void runUdp()  {
    try {
      DatagramSocket socket = new DatagramSocket(null);
      socket.setReuseAddress(true);
      socket.bind(new InetSocketAddress("0.0.0.0", dnsPort));
      // socket.setSoTimeout(0);
      logger.info("Dns server LOADED, port: " + dnsPort);
      byte[] in = new byte[UDP_SIZE];

      // Read the request
      DatagramPacket indp = new DatagramPacket(in, UDP_SIZE);

      while (running.get()) {
        indp.setLength(in.length);

        try {
          socket.receive(indp);
          var inCopy = in.clone();
          var inAddress = indp.getAddress();
          var inPort = indp.getPort();
          executorService.submit(()->{
            resolveAll(inAddress,inPort, socket, inCopy);
          });

        } catch (InterruptedIOException e) {
          continue;
        }
      }
    }catch (Exception ex){
      logger.error("Error running udp thread",ex);
    }
  }


  private void resolveAll(InetAddress inAddress,int inPort, DatagramSocket socket, byte[] in) {
    try {
      // Build the response
      byte[] resp = buildResponse(in);
      logger.debug("SENDING RESPONSE UDP");
      DatagramPacket outdp =
              new DatagramPacket(resp, resp.length, inAddress, inPort);
      socket.send(outdp);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }



  @SuppressWarnings("InfiniteLoopStatement")
  public void run() throws IOException, InterruptedException {
    var udpThread = new Thread(() -> runUdp());
    var tcpThread = new Thread(() -> runTcp());
    running.set(true);
    udpThread.start();
    tcpThread.start();
    Thread.sleep(1000);

    while(
            udpThread.isAlive() &&
                    tcpThread.isAlive()) {
      Thread.sleep(60000);
    }
    running.set(false);
    while(
            udpThread.isAlive() ||
                    tcpThread.isAlive()) {
      Thread.sleep(1000);
    }
  }

  byte[] buildErrorMessage(Header header, int rcode, org.xbill.DNS.Record question) {
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




  private byte[] buildResponse(byte[] in) throws IOException {
    Message request;
    request = new Message(in);
    Message response = new Message(request.getHeader().getID());
    String requestedDomain = request.getQuestion().getName().toString(true);

    logger.debug("Requested domain " + requestedDomain);

    List<String> ips = new ArrayList<>();

    var splitted= requestedDomain.split("\\.");
    var containsAtLeastOneInternal = false;
    var endsWith = false;
    var isUpperCase = requestedDomain.toUpperCase(Locale.ROOT).equals(requestedDomain);
    if(splitted.length>=3){
      var occurr = (splitted[splitted.length-2]+"."+ splitted[splitted.length-1]).toLowerCase(Locale.ROOT);
      containsAtLeastOneInternal = StringUtils.countOccurrencesOf(requestedDomain.toLowerCase(Locale.ROOT),"."+occurr+".")>=1;
      endsWith = requestedDomain.toLowerCase(Locale.ROOT).endsWith("."+occurr);
    }
    response.addRecord(request.getQuestion(), Section.QUESTION);


    if(!(containsAtLeastOneInternal && endsWith) && !isUpperCase) {
      if (!requestedDomain.equals(blocker.apply(requestedDomain))) {
        ips = this.multiResolver.resolve(blocker.apply(requestedDomain));
      }
    }

    byte[] resp;
    if (ips.size() > 0) {
      for (String ip : ips) {
        logger.debug("FOUNDED IP " + ip + " FOR " + requestedDomain);
        // Add answers as needed
        response.addRecord(
                Record.fromString(Name.root, Type.A, DClass.IN, 86400, ip, Name.fromString(requestedDomain)),
                Section.ANSWER);
      }
      resp = response.toWire();
    } else {
      resp = errorMessage(request, Rcode.NXDOMAIN);
    }
    return resp;
  }

  public void setBlocker(Function<String,String> blocker) {
    this.blocker = blocker;
  }
}
