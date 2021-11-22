package org.kendar.dns;

import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class DnsDirectCaller {
  private final Logger logger;

  public DnsDirectCaller(LoggerBuilder builder) {
    logger = builder.build(DnsDirectCaller.class);
  }

  private void testDnsServer(String DNS_SERVER_ADDRESS) throws Exception {
    int DNS_SERVER_PORT = 53;
    logger.debug("Testing " + DNS_SERVER_ADDRESS);
    String domain = "google.com";
    InetAddress ipAddress = InetAddress.getByName(DNS_SERVER_ADDRESS);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);

    // *** Build a DNS Request Frame ****

    // Identifier: A 16-bit identification field generated by the device that creates the DNS query.
    // It is copied by the server into the response, so it can be used by that device to match that
    // query to the corresponding reply received from a DNS server. This is used in a manner similar
    // to how the Identifier field is used in many of the ICMP message types.
    dos.writeShort(0x1234);

    // Write Query Flags
    dos.writeShort(0x0100);

    // Question Count: Specifies the number of questions in the Question section of the message.
    dos.writeShort(0x0001);

    // Answer Record Count: Specifies the number of resource records in the Answer section of the
    // message.
    dos.writeShort(0x0000);

    // Authority Record Count: Specifies the number of resource records in the Authority section of
    // the message. (“NS” stands for “name server”)
    dos.writeShort(0x0000);

    // Additional Record Count: Specifies the number of resource records in the Additional section
    // of the message.
    dos.writeShort(0x0000);

    String[] domainParts = domain.split("\\.");
    logger.debug(domain + " has " + domainParts.length + " parts");

    for (int i = 0; i < domainParts.length; i++) {
      logger.debug("Writing: " + domainParts[i]);
      byte[] domainBytes = domainParts[i].getBytes(StandardCharsets.UTF_8);
      dos.writeByte(domainBytes.length);
      dos.write(domainBytes);
    }

    // No more parts
    dos.writeByte(0x00);

    // Type 0x01 = A (Host Request)
    dos.writeShort(0x0001);

    // Class 0x01 = IN
    dos.writeShort(0x0001);

    byte[] dnsFrame = baos.toByteArray();

    logger.debug("Sending: " + dnsFrame.length + " bytes");
    StringBuilder debugString = new StringBuilder();
    for (int i = 0; i < dnsFrame.length; i++) {
      debugString.append("0x").append(String.format("%x", dnsFrame[i])).append(" ");
    }
    logger.debug(debugString.toString());

    // *** Send DNS Request Frame ***
    DatagramSocket socket = new DatagramSocket();
    DatagramPacket dnsReqPacket =
        new DatagramPacket(dnsFrame, dnsFrame.length, ipAddress, DNS_SERVER_PORT);
    socket.send(dnsReqPacket);

    // Await response from DNS server
    byte[] buf = new byte[1024];
    DatagramPacket packet = new DatagramPacket(buf, buf.length);
    socket.setSoTimeout(2000);
    socket.receive(packet);

    logger.debug("Received: " + packet.getLength() + " bytes");

    debugString = new StringBuilder();
    for (int i = 0; i < packet.getLength(); i++) {
      debugString.append(" 0x").append(String.format("%x", buf[i])).append(" ");
    }
    logger.debug(debugString.toString());

    DataInputStream din = new DataInputStream(new ByteArrayInputStream(buf));
    logger.debug("Transaction ID: 0x" + String.format("%x", din.readShort()));
    logger.debug("Flags: 0x" + String.format("%x", din.readShort()));
    logger.debug("Questions: 0x" + String.format("%x", din.readShort()));
    logger.debug("Answers RRs: 0x" + String.format("%x", din.readShort()));
    logger.debug("Authority RRs: 0x" + String.format("%x", din.readShort()));
    logger.debug("Additional RRs: 0x" + String.format("%x", din.readShort()));

    int recLen;
    while ((recLen = din.readByte()) > 0) {
      byte[] record = new byte[recLen];

      for (int i = 0; i < recLen; i++) {
        record[i] = din.readByte();
      }

      logger.debug("Record: " + new String(record, StandardCharsets.UTF_8));
    }

    logger.debug("Record Type: 0x" + String.format("%x", din.readShort()));
    logger.debug("Class: 0x" + String.format("%x", din.readShort()));

    logger.debug("Field: 0x" + String.format("%x", din.readShort()));
    logger.debug("Type: 0x" + String.format("%x", din.readShort()));
    logger.debug("Class: 0x" + String.format("%x", din.readShort()));
    logger.debug("TTL: 0x" + String.format("%x", din.readInt()));

    short addrLen = din.readShort();
    logger.debug("Len: 0x" + String.format("%x", addrLen));

    debugString = new StringBuilder(("Address: "));
    for (int i = 0; i < addrLen; i++) {
      debugString.append("").append(String.format("%d", (din.readByte() & 0xFF))).append(".");
    }
    logger.debug(debugString.toString());
  }
}
