package org.kendar.servers.dns;

import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;

@Component
public class DnsMultiResolverImpl implements  DnsMultiResolver{
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);
    private final Logger logger;
    @Value("${dns.extraServers}")
    private String[] extraServers;
    @Value("${localhost.name:localhost.dev.it}")
    private String localHostName;

    @Value("${dns.logging.query:false}")
    private boolean dnsLogginQuery;
    private ConcurrentHashMap<String,List<String>> domains = new ConcurrentHashMap<>();

    private List<PatternItem> dnsRecords = new ArrayList<>();
    private Environment environment;

    public DnsMultiResolverImpl(Environment environment, LoggerBuilder loggerBuilder){
        this.environment = environment;
        this.logger = loggerBuilder.build(DnsMultiResolverImpl.class);
    }

    @PostConstruct
    public void init(){
        String hostsFile="";
        dnsRecords.add(new PatternItem(localHostName,"127.0.0.1"));
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
    }

    @Override
    public List<String> resolveLocal(String requestedDomain) {
        var data = new ArrayList<String>();
        if(requestedDomain.equalsIgnoreCase("localhost")){
            data.add("127.0.0.1");
            return data;
        }else if(requestedDomain.equalsIgnoreCase("1.0.0.127.in-addr.arpa")){
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
                    data.add(item.getIp());
                }
            }
        }
        var result = new ArrayList<>(data);
        if(dnsLogginQuery) {
            if(result.size()>0){
                logger.info("Resloved local "+requestedDomain+result.get(0));
            }
        }
        return data;
    }

    @Override
    public List<String> resolveRemote(String requestedDomain) {
        var data = new HashSet<String>();
        List<Callable<List<String>>> runnables = new ArrayList<>();
        for(int i = 0; i< extraServers.length; i++){
            var serverToCall = extraServers[i];
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
        while(finished!=0){
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
        return resolveLocal(requestedDomain);
    }
}
