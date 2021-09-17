package org.kendar.dns;

import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Component
public class DnsMultiResolverImpl implements DnsMultiResolver {
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);
    private final Logger logger;
    private final String localHostAddress;
    @Value("${dns.extraServers:8.8.8.8}")
    private String[] extraServers;
    private List<String> extraServersReal = new ArrayList<>();
    @Value("${localhost.name:www.local.org}")
    private String localHostName;

    @Value("${dns.logging.query:false}")
    private boolean dnsLogginQuery;
    private ConcurrentHashMap<String,List<String>> domains = new ConcurrentHashMap<>();
    private Set<String> uncallable = new HashSet<>();

    private List<PatternItem> dnsRecords = new ArrayList<>();
    private Environment environment;

    public DnsMultiResolverImpl(Environment environment, LoggerBuilder loggerBuilder){
        this.environment = environment;
        this.logger = loggerBuilder.build(DnsMultiResolverImpl.class);
        this.localHostAddress = getLocalHostLANAddress();
        System.out.println(localHostAddress);
    }

    public List<String> getExtraServers(){
        return Arrays.asList(extraServers);
    }

    private Pattern ipPattern = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");
    @PostConstruct
    public void init(){
        String hostsFile="";
        dnsRecords.add(new PatternItem(localHostName,localHostAddress));
        for(int i=0;i<1000;i++){
            var index = "dns.resolve."+Integer.toString(i);
            var dns = environment.getProperty(index);
            if(dns != null){
                var parts= dns.split("\\s+");
                var pi = new PatternItem(parts[0],parts[1]);
                hostsFile += pi.writeHostsLine()+"\n";
                dnsRecords.add(pi);
            }
        }
        if(hostsFile.length()>0){
            try {
                var myWriter = new FileWriter("hosts.txt");
                myWriter.write(hostsFile);
                myWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < extraServers.length; i++) {
            String server = extraServers[i];
            try {
                Matcher matcher = ipPattern.matcher(server);
                if(matcher.matches()){
                    extraServersReal.add(server);
                }else{
                    var namedDns = resolve(server);
                    if(namedDns.size()==0){
                        logger.error("Not found named DNS "+server);
                    }else {
                        logger.info("Resolved named DNS "+server+":"+namedDns.get(0));
                        extraServersReal.add(0,namedDns.get(0));
                    }
                }
            } catch (PatternSyntaxException ex) {

            }
        }
    }

    private String getLocalHostLANAddress()  {
        try {
            InetAddress candidateAddress = null;
            // Iterate all NICs (network interface cards)...
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // Iterate all IP addresses assigned to each card...
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {

                        if (inetAddr.isSiteLocalAddress()) {
                            // Found non-loopback site-local address. Return it immediately...
                            return inetAddr.getHostAddress();
                        }
                        else if (candidateAddress == null) {
                            // Found non-loopback address, but not necessarily site-local.
                            // Store it as a candidate to be returned if site-local address is not subsequently found...
                            candidateAddress = inetAddr;
                            // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
                            // only the first. For subsequent iterations, candidate will be non-null.
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                // We did not find a site-local address, but we found some other non-loopback address.
                // Server might have a non-site-local address assigned to its NIC (or it might be running
                // IPv6 which deprecates the "site-local" concept).
                // Return this non-loopback candidate address...
                return candidateAddress.getHostAddress();
            }
            // At this point, we did not find a non-loopback address.
            // Fall back to returning whatever InetAddress.getLocalHost() returns...
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                return null;
            }
            return jdkSuppliedAddress.getHostAddress();
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<String> resolveLocal(String requestedDomain) {
        var data = new ArrayList<String>();
        if(requestedDomain.equalsIgnoreCase("localhost")){
            data.add("127.0.0.1");
            return data;
        }else if(requestedDomain.endsWith("in-addr.arpa")){
            data.add("127.0.0.1");
            return data;
        }else if(requestedDomain.endsWith("ip6.arpa")){
            data.add("127.0.0.1");
            return data;
        }else {
            for (int i = 0; i < dnsRecords.size(); i++) {
                var item = dnsRecords.get(i);
                if (item.match(requestedDomain)) {
                    if(dnsLogginQuery) {
                        logger.info("Pattern " + item.getIp());
                        logger.info("Request " + requestedDomain);
                        logger.info("Ip " + item.getIp());

                    }
                    if(item.getIp().equalsIgnoreCase("127.0.0.1")){
                        data.add(this.localHostAddress);
                    }else{
                        data.add(item.getIp());
                    }
                }
            }
        }
        var result = new ArrayList<>(data);
        if(dnsLogginQuery) {
            if(result.size()>0){
                logger.info("Resolved local "+requestedDomain+result.get(0));
            }
        }
        return data;
    }

    private void testDnsServer(String DNS_SERVER_ADDRESS) throws Exception {
        int DNS_SERVER_PORT=53;
        logger.debug("Testing "+DNS_SERVER_ADDRESS);
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

        // Answer Record Count: Specifies the number of resource records in the Answer section of the message.
        dos.writeShort(0x0000);

        // Authority Record Count: Specifies the number of resource records in the Authority section of
        // the message. (“NS” stands for “name server”)
        dos.writeShort(0x0000);

        // Additional Record Count: Specifies the number of resource records in the Additional section of the message.
        dos.writeShort(0x0000);

        String[] domainParts = domain.split("\\.");
        logger.debug(domain + " has " + domainParts.length + " parts");

        for (int i = 0; i<domainParts.length; i++) {
            logger.debug("Writing: " + domainParts[i]);
            byte[] domainBytes = domainParts[i].getBytes("UTF-8");
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
        String debugString = "";
        for (int i =0; i< dnsFrame.length; i++) {
            debugString+=("0x" + String.format("%x", dnsFrame[i]) + " " );
        }
        logger.debug(debugString);

        // *** Send DNS Request Frame ***
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket dnsReqPacket = new DatagramPacket(dnsFrame, dnsFrame.length, ipAddress, DNS_SERVER_PORT);
        socket.send(dnsReqPacket);

        // Await response from DNS server
        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.setSoTimeout(2000);
        socket.receive(packet);

        logger.debug("Received: " + packet.getLength() + " bytes");

        debugString="";
        for (int i = 0; i < packet.getLength(); i++) {
            debugString+=(" 0x" + String.format("%x", buf[i]) + " " );
        }
        logger.debug(debugString);


        DataInputStream din = new DataInputStream(new ByteArrayInputStream(buf));
        logger.debug("Transaction ID: 0x" + String.format("%x", din.readShort()));
        logger.debug("Flags: 0x" + String.format("%x", din.readShort()));
        logger.debug("Questions: 0x" + String.format("%x", din.readShort()));
        logger.debug("Answers RRs: 0x" + String.format("%x", din.readShort()));
        logger.debug("Authority RRs: 0x" + String.format("%x", din.readShort()));
        logger.debug("Additional RRs: 0x" + String.format("%x", din.readShort()));

        int recLen = 0;
        while ((recLen = din.readByte()) > 0) {
            byte[] record = new byte[recLen];

            for (int i = 0; i < recLen; i++) {
                record[i] = din.readByte();
            }

            logger.debug("Record: " + new String(record, "UTF-8"));
        }

        logger.debug("Record Type: 0x" + String.format("%x", din.readShort()));
        logger.debug("Class: 0x" + String.format("%x", din.readShort()));

        logger.debug("Field: 0x" + String.format("%x", din.readShort()));
        logger.debug("Type: 0x" + String.format("%x", din.readShort()));
        logger.debug("Class: 0x" + String.format("%x", din.readShort()));
        logger.debug("TTL: 0x" + String.format("%x", din.readInt()));

        short addrLen = din.readShort();
        logger.debug("Len: 0x" + String.format("%x", addrLen));

        debugString = ("Address: ");
        for (int i = 0; i < addrLen; i++ ) {
            debugString+=("" + String.format("%d", (din.readByte() & 0xFF)) + ".");
        }
        logger.debug(debugString);

    }

    public void verify(){
        /*logger.info("Verify Dns servers");
        List<Callable<List<String>>> runnables = new ArrayList<>();
        for(int i = 0; i< extraServersReal.size(); i++){
            var serverToCall = extraServersReal.get(i);

            try {
                testDnsServer(serverToCall);
            }catch(Exception ex){
                //Ignore localhost server when not working
                if(serverToCall.startsWith("127")||serverToCall.equalsIgnoreCase(localHostAddress)){
                    uncallable.add(serverToCall);
                    logger.info("Inhibited DNS Server "+serverToCall);
                }
            }
        }*/
    }

    @Override
    public List<String> resolveRemote(String requestedDomain) {
        var data = new HashSet<String>();
        List<Callable<List<String>>> runnables = new ArrayList<>();
        for(int i = 0; i< extraServersReal.size(); i++){
            var serverToCall = extraServersReal.get(i);
            if(uncallable.contains(serverToCall)) continue;
            //logger.info(serverToCall+" "+requestedDomain);
            var runnable = new DnsRunnable(serverToCall,requestedDomain);
            runnables.add(runnable);
        }
        List<Future<List<String>>> futures = new ArrayList<>();
        try {
            futures = executorService.invokeAll(runnables);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int finished = futures.size();
        //This method returns the time in millis
        long timeMilli = new Date().getTime();
        long timeEnd = timeMilli+500;
        while(finished!=0){
            if(timeEnd<=new Date().getTime()){
                //System.out.println("================");
                for(var current:futures){
                    current.cancel(true);
                }
                break;
            }
            finished = futures.size();
            for(var current:futures){
                if(current.isCancelled()) {
                    finished--;
                }else if(current.isDone()){
                    finished--;
                    try {
                        var currentData = current.get();
                        if(currentData.size()==0){
                            continue;
                        }
                        for (String address: current.get()) {
                            if(!data.contains(address)){
                                data.add(address);
                            }
                        }
                        for (var future: futures) {
                            if(!future.isDone()){
                                future.cancel(true);
                            }
                        }
                        futures.clear();
                        if(data.size()>0) {
                            domains.put(requestedDomain, new ArrayList<>(data));
                        }else if(domains.containsKey(requestedDomain)){
                            domains.remove(requestedDomain);
                        }
                        return new ArrayList<>(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        var result = new ArrayList<>(data);
        if(dnsLogginQuery) {
            if(result.size()>0){
                logger.info("Resloved remote "+requestedDomain+result.get(0));
            }
        }
        return result;
    }

    @Override
    public List<String> resolve(String requestedDomain) {
        if(domains.containsKey(requestedDomain)){
            return domains.get(requestedDomain);
        }

        var localData = resolveLocal(requestedDomain);
        if(localData.size()>0){
            return localData;
        }
        return resolveRemote(requestedDomain);
    }
}
