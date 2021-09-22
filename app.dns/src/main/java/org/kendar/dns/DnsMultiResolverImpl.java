package org.kendar.dns;

import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.servers.dns.DnsServerDescriptor;
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
import java.util.concurrent.atomic.AtomicReference;
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
    //private List<String> extraServersReal = new ArrayList<>();
    private AtomicReference<List<DnsServerDescriptor>> extraServersReal = new AtomicReference<>();
    @Value("${localhost.name:www.local.org}")
    private String localHostName;

    private ConcurrentHashMap<String,Integer> blockedLoops = new ConcurrentHashMap<>();

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
        extraServersReal.set(new ArrayList<>());
    }

    public List<DnsServerDescriptor> getExtraServers(){
        return extraServersReal.get();
    }

    public void setExtraServers(List<DnsServerDescriptor>  extraServers){
        extraServersReal.set(extraServers);
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

        var extraServersRealList = new ArrayList<DnsServerDescriptor>();
        extraServersReal.set(extraServersRealList);
        for (int i = 0; i < extraServers.length; i++) {
            String server = extraServers[i];
            try {
                Matcher matcher = ipPattern.matcher(server);
                if(matcher.matches()){
                    var desc = new DnsServerDescriptor();
                    desc.setIp(server);
                    desc.setEnabled(true);
                    extraServersRealList.add(desc);
                }else{
                    var namedDns = resolve(server,false);
                    if(namedDns.size()==0){
                        logger.error("Not found named DNS "+server);
                    }else {
                        logger.info("Resolved named DNS "+server+":"+namedDns.get(0));

                        var desc = new DnsServerDescriptor();
                        desc.setName(server);
                        desc.setIp(namedDns.get(0));
                        desc.setEnabled(true);
                        extraServersRealList.add(0,desc);
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
    public List<String> resolveRemote(String requestedDomain,boolean fromLocalHost) {
        var computed = 1;
        if(fromLocalHost) {
            if (blockedLoops.containsKey(requestedDomain)) {
                var val = blockedLoops.get(requestedDomain);
                blockedLoops.put(requestedDomain, val + 1);
                computed = val + 1;
            } else {
                blockedLoops.put(requestedDomain, computed);
            }
            if(computed>4){
                logger.info("Blocked dns Loop "+requestedDomain);
                return new ArrayList<>();
            }
        }
        var data = new HashSet<String>();
        List<Callable<List<String>>> runnables = new ArrayList<>();
        var extraServersList = getExtraServers();
        for(int i = 0; i< extraServersList.size(); i++){
            var serverToCall = extraServersList.get(i);
            if(uncallable.contains(serverToCall.getIp())) continue;
            //logger.info(serverToCall+" "+requestedDomain);
            var runnable = new DnsRunnable(serverToCall.getIp(),requestedDomain);
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
        long timeEnd = timeMilli+2000;
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
    public List<String> resolve(String requestedDomain,boolean fromLocalhost) {
        if(domains.containsKey(requestedDomain)){
            return domains.get(requestedDomain);
        }

        var localData = resolveLocal(requestedDomain);
        if(localData.size()>0){
            return localData;
        }
        return resolveRemote(requestedDomain,fromLocalhost);
    }
}
