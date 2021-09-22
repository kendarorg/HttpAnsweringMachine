package org.kendar.servers.proxy;

import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.servers.http.Request;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

@Component
public class SimpleProxyHandlerImpl implements SimpleProxyHandler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Logger logger;
    private DnsMultiResolver multiResolver;
    private Environment environment;
    private ConcurrentLinkedQueue<RemoteServerStatus> proxies = new ConcurrentLinkedQueue<>();

    public SimpleProxyHandlerImpl(LoggerBuilder loggerBuilder,DnsMultiResolver multiResolver, Environment environment){
        this.multiResolver = multiResolver;
        this.environment = environment;
        logger = loggerBuilder.build(SimpleProxyHandlerImpl.class);
    }

    public List<RemoteServerStatus> getProxies(){
        return Arrays.asList((RemoteServerStatus[])proxies.toArray());
    }
    @PostConstruct
    public void init(){
        for(int i=0;i<1000;i++){
            var index = "simpleproxy."+Integer.toString(i)+".";
            if(environment.getProperty(index+"when")==null){
                break;
            }
            var data = new RemoteServerStatus(
                    environment.getProperty(index+"when"),
                    environment.getProperty(index+"where"),
                    environment.getProperty(index+"test")
            );
            proxies.add(data);
        }
        scheduler.scheduleAtFixedRate(() -> {
            doLog();
            var data = Arrays.asList( proxies.toArray());
            for (int i = 0; i< data.size();i++)  {
                checkRemoteMachines((RemoteServerStatus) data.get(i));
            }

        },1000,5*60*1000, TimeUnit.MILLISECONDS);

        logger.info("Simple proxyes LOADED");
    }

    private boolean startedOnce = false;
    private void doLog() {
        if(!startedOnce){
            startedOnce=true;
            logger.info("Simple proxyes CHECKED");
        }

    }

    private void checkRemoteMachines(RemoteServerStatus value) {
        var data = multiResolver.resolveRemote(value.getTest(),false);
        if(data!=null && data.size()>0){
            try {
                var inetAddress = InetAddress.getByName(data.get(0));
                value.setRunning(inetAddress.isReachable(100));
            } catch (IOException e) {
                value.setRunning(false);
            }
        }else{
            value.setRunning(false);
        }
    }

    public boolean ping(String host){
        try {
            var pingable = false;
            Socket t = new Socket(host, 7);
            DataInputStream dis = new DataInputStream(t.getInputStream());
            PrintStream ps = new PrintStream(t.getOutputStream());
            ps.println("Hello");
            @SuppressWarnings("deprecation") String str = dis.readLine();
            if(str.equals("Hello")) {
                pingable = true;
            } else {
                pingable = false;
            }
            t.close();
            return pingable;
        }catch (IOException e) {
            return false;
        }
    }

    public Request translate(Request source) throws MalformedURLException {
        var realSrc = source.getProtocol()+"://"+source.getHost()+source.getPath();
        var data = Arrays.asList( proxies.toArray());
        for (int i=0;i<data.size();i++)  {
            var status=(RemoteServerStatus)data.get(i);
            if(realSrc.startsWith(status.getWhen()) && status.isRunning()){
                realSrc = realSrc.replace(status.getWhen(),status.getWhere());
                var url = new URL(realSrc);
                if(url.getProtocol().equalsIgnoreCase("https") && url.getPort()!=443){
                    source.setPort(url.getPort());
                    source.setProtocol("https");
                }else if(url.getProtocol().equalsIgnoreCase("http") && url.getPort()!=80){
                    source.setPort(url.getPort());
                    source.setProtocol("http");
                }else if(url.getProtocol().equalsIgnoreCase("https") && url.getPort()==443){
                    source.setPort(-1);
                    source.setProtocol("https");
                }else if(url.getProtocol().equalsIgnoreCase("http") && url.getPort()==80){
                    source.setPort(-1);
                    source.setProtocol("http");
                }
                source.setHost(url.getHost());
                source.setPath(url.getPath());
                return source;
            }
        }
        return source;
    }
}
