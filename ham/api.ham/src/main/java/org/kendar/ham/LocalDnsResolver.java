package org.kendar.ham;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LocalDnsResolver {

    public static void solve(String dnsServer,int dnsPort,String domain) throws IOException {

        InetAddress ipAddress = InetAddress.getByName(dnsServer);
        int DNS_SERVER_PORT = dnsPort;

        Random random = new Random();
        short ID = (short)random.nextInt(32767);
        System.out.println(ID);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        short requestFlags = Short.parseShort("0000000100000000", 2);
        ByteBuffer byteBuffer = ByteBuffer.allocate(2).putShort(requestFlags);
        byte[] flagsByteArray = byteBuffer.array();

        short QDCOUNT = 1;
        short ANCOUNT = 0;
        short NSCOUNT = 0;
        short ARCOUNT = 0;

        dataOutputStream.writeShort(ID);
        dataOutputStream.write(flagsByteArray);
        dataOutputStream.writeShort(QDCOUNT);
        dataOutputStream.writeShort(ANCOUNT);
        dataOutputStream.writeShort(NSCOUNT);
        dataOutputStream.writeShort(ARCOUNT);
        String[] domainParts = domain.split("\\.");

        for (int i = 0; i < domainParts.length; i++) {
            byte[] domainBytes = domainParts[i].getBytes(StandardCharsets.UTF_8);
            dataOutputStream.writeByte(domainBytes.length);
            dataOutputStream.write(domainBytes);
        }
        // No more parts
        dataOutputStream.writeByte(0);
        // Type 0x01 = A (Host Request)
        dataOutputStream.writeShort(1);
        // Class 0x01 = IN
        dataOutputStream.writeShort(1);

        byte[] dnsFrame = byteArrayOutputStream.toByteArray();

        System.out.println("SendataInputStreamg: " + dnsFrame.length + " bytes");
        for (int i = 0; i < dnsFrame.length; i++) {
            System.out.print(String.format("%s", dnsFrame[i]) + " ");
        }

        DatagramSocket socket = new DatagramSocket();
        DatagramPacket dnsReqPacket = new DatagramPacket(dnsFrame, dnsFrame.length, ipAddress, DNS_SERVER_PORT);
        socket.send(dnsReqPacket);


        byte[] response = new byte[1024];
        DatagramPacket packet = new DatagramPacket(response, response.length);
        socket.receive(packet);

        System.out.println("\n\nReceived: " + packet.getLength() + " bytes");
        for (int i = 0; i < packet.getLength(); i++) {
            System.out.print(String.format("%s", response[i]) + " ");
        }
        System.out.println("\n");

        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(response));
        System.out.println("\n\nStart response decode");
        System.out.println("Transaction ID: " + dataInputStream.readShort()); // ID
        short flags = dataInputStream.readByte();
        int QR = (flags & 0b10000000) >>> 7;
        int opCode = ( flags & 0b01111000) >>> 3;
        int AA = ( flags & 0b00000100) >>> 2;
        int TC = ( flags & 0b00000010) >>> 1;
        int RD = flags & 0b00000001;
        System.out.println("QR "+QR);
        System.out.println("Opcode "+opCode);
        System.out.println("AA "+AA);
        System.out.println("TC "+TC);
        System.out.println("RD "+RD);
        flags = dataInputStream.readByte();
        int RA = (flags & 0b10000000) >>> 7;
        int Z = ( flags & 0b01110000) >>> 4;
        int RCODE = flags & 0b00001111;
        System.out.println("RA "+RA);
        System.out.println("Z "+ Z);
        System.out.println("RCODE " +RCODE);

        QDCOUNT = dataInputStream.readShort();
        ANCOUNT = dataInputStream.readShort();
        NSCOUNT = dataInputStream.readShort();
        ARCOUNT = dataInputStream.readShort();

        System.out.println("Questions: " + String.format("%s",QDCOUNT ));
        System.out.println("Answers RRs: " + String.format("%s", ANCOUNT));
        System.out.println("Authority RRs: " + String.format("%s", NSCOUNT));
        System.out.println("Additional RRs: " + String.format("%s", ARCOUNT));

        String QNAME = "";
        int recLen;
        while ((recLen = dataInputStream.readByte()) > 0) {
            byte[] record = new byte[recLen];
            for (int i = 0; i < recLen; i++) {
                record[i] = dataInputStream.readByte();
            }
            QNAME = new String(record, StandardCharsets.UTF_8);
        }
        short QTYPE = dataInputStream.readShort();
        short QCLASS = dataInputStream.readShort();
        System.out.println("Record: " + QNAME);
        System.out.println("Record Type: " + String.format("%s", QTYPE));
        System.out.println("Class: " + String.format("%s", QCLASS));

        System.out.println("\n\nstart answer, authority, and additional sections\n");

        byte firstBytes = dataInputStream.readByte();
        int firstTwoBits = (firstBytes & 0b11000000) >>> 6;

        ByteArrayOutputStream label = new ByteArrayOutputStream();
        Map<String, String> domainToIp = new HashMap<>();

        for(int i = 0; i < ANCOUNT; i++) {
            if(firstTwoBits == 3) {
                byte currentByte = dataInputStream.readByte();
                boolean stop = false;
                byte[] newArray = Arrays.copyOfRange(response, currentByte, response.length);
                DataInputStream sectionDataInputStream = new DataInputStream(new ByteArrayInputStream(newArray));
                ArrayList<Integer> RDATA = new ArrayList<>();
                ArrayList<String> DOMAINS = new ArrayList<>();
                while(!stop) {
                    byte nextByte = sectionDataInputStream.readByte();
                    if(nextByte != 0) {
                        byte[] currentLabel = new byte[nextByte];
                        for(int j = 0; j < nextByte; j++) {
                            currentLabel[j] = sectionDataInputStream.readByte();
                        }
                        label.write(currentLabel);
                    } else {
                        stop = true;
                        short TYPE = dataInputStream.readShort();
                        short CLASS = dataInputStream.readShort();
                        int TTL = dataInputStream.readInt();
                        int RDLENGTH = dataInputStream.readShort();
                        for(int s = 0; s < RDLENGTH; s++) {
                            int nx = dataInputStream.readByte() & 255;// and with 255 to
                            RDATA.add(nx);
                        }

                        System.out.println("Type: " + TYPE);
                        System.out.println("Class: " + CLASS);
                        System.out.println("Time to live: " + TTL);
                        System.out.println("Rd Length: " + RDLENGTH);
                    }

                    DOMAINS.add(label.toString(StandardCharsets.UTF_8));
                    label.reset();
                }

                StringBuilder ip = new StringBuilder();
                StringBuilder domainSb = new StringBuilder();
                for(Integer ipPart:RDATA) {
                    ip.append(ipPart).append(".");
                }

                for(String domainPart:DOMAINS) {
                    if(!domainPart.equals("")) {
                        domainSb.append(domainPart).append(".");
                    }
                }
                String domainFinal = domainSb.toString();
                String ipFinal = ip.toString();
                domainToIp.put(ipFinal.substring(0, ipFinal.length()-1), domainFinal.substring(0, domainFinal.length()-1));

            }else if(firstTwoBits == 0){
                System.out.println("It's a label");
            }

            firstBytes = dataInputStream.readByte();
            firstTwoBits = (firstBytes & 0b11000000) >>> 6;
        }

        domainToIp.forEach((key, value) -> System.out.println(key + " : " + value));
    }
}