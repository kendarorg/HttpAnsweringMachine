package org.kendar.dns;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.kendar.dns.DNSTunnelConstants.Class.IN;

public class ByteDnsServer {
    static final String SEPARATOR = ":";
    static final String STOP = "stop";
    static final short QR_RESPONSE = (short) (1 << 15);
    static final short AA_BIT = 1 << 10;

    static final CrockfordBase32 BASE32 = new CrockfordBase32();

    public static byte[] buildResponse(byte[] inp) {
        try {
            var in = new DataInputStream(new ByteArrayInputStream(inp));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            var out = new DataOutputStream(bos);
            short id = in.readShort();

            // skip flags
            in.skipBytes(2);

            // read questions count (QDCOUNT)
            short qdcount = in.readShort();
            if (qdcount < 1) {
                throw new IOException("Wrong QDCOUNT: " + qdcount);
            }

            // skip ANCOUNT, NSCOUNT, ARCOUNT
            in.skipBytes(6);

            // read first question
            int len;
            List<byte[]> components = new ArrayList<>();
            while ((len = in.readByte()) > 0) {
                byte[] component = new byte[len];
                in.read(component, 0, len);
                components.add(component);
            }

            if (components.size() < 2) {
                throw new IOException("Wrong QNAME");
            }

            // TODO: check domain name

            short qtype = in.readShort();
            short qclass = in.readShort();

            processData(components.get(0));

            // build DNS response
            // write ID
            out.writeShort(id);

            // write flags
            // set QR = 1 which means this message is a response
            // set AA = 1 which means that the responding name server is an
            // authority for the domain name
            short flags = 0;
            flags = (short) (flags | QR_RESPONSE | AA_BIT);

            out.writeShort(flags);

            // write QDCOUNT = 1
            out.writeShort(1);

            // TODO: if an error occured while sending a command,
            //       the command should be returned to the queue
//            String command;
//            synchronized (commands) {
//                command = commands.poll();
//            }
//
//            short ancount = (short) (command == null ? 1 : 2);
            short ancount=1;


            // write ANCOUNT
            out.writeShort(ancount);

            // write NSCOUNT
            out.writeShort(0);

            // write ARCOUNT
            out.writeShort(0);

            // write the question (Java DNS client rejects responses without it)
            for (byte[] component : components) {
                out.write(component.length);
                out.write(component, 0, component.length);
            }
            out.write(0);
            out.writeShort(qtype);
            out.writeShort(qclass);

            // write an answer, A IN 1.2.3.4
            out.write(buildAnswerRecord(components, new byte[]{1, 2, 3, 4}));

            // send a command if available
//            if (command != null) {
//                debug("send a command: " + command);
//                out.write(buildTxtRecord(components, command));
//            }

            out.flush();
            byte[] buf = bos.toByteArray();
            return buf;
        }catch(Exception ex){
            throw new RuntimeException();
        }
    }

    private static byte[] buildAnswerRecord(List<byte[]> name, byte[] data)
            throws IOException {

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(bos)) {

            // write NAME
            for (byte[] component : name) {
                out.write(component.length);
                out.write(component, 0, component.length);
            }
            out.write(0);

            // write QTYPE and QCLASS
            out.writeShort(DNSTunnelConstants.QType.A.getValue());
            out.writeShort(IN.getValue());

            // write TTL=0
            out.write(new byte[] {0, 0, 0, 0});

            // write an IP address (RDLENGTH and RDATA)
            out.writeShort(data.length);
            out.write(data, 0, data.length);

            out.flush();
            return bos.toByteArray();
        }
    }

    private byte[] buildTxtRecord(List<byte[]> name, String text)
            throws IOException {

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(bos)) {

            // write NAME
            for (byte[] component : name) {
                out.write(component.length);
                out.write(component, 0, component.length);
            }
            out.write(0);

            // Write QTYPE and QCLASS
            out.writeShort(DNSTunnelConstants.QType.TXT.getValue());
            out.writeShort(IN.getValue());

            // write TTL=0
            out.write(new byte[] {0, 0, 0, 0});

            // write TXT data (RDLENGTH and RDATA)
            byte[] data = text.getBytes("US-ASCII");

            // RDLENGTH
            out.writeShort(data.length + 1);

            // TXT length and text
            out.write(data.length);
            out.write(data, 0, data.length);

            out.flush();
            return bos.toByteArray();
        }
    }

    private static void processData(byte[] bytes) throws IOException {
        String data = BASE32.decodeToString(bytes);
        debug("processData(): decoded data = " + data);
        int pos = data.indexOf(SEPARATOR);
        if (pos < 0) {
            throw new IOException("Wrong request, couldn't find a separator");
        }
        String output = data.substring(pos + 1);
        if (!output.isEmpty()) {
            print(output);
        } else {
            debug("processData(): no output");
        }
    }

    private static void print(String output) {
    }

    private static void debug(String s) {

    }
}
