package org.kendar;

import org.kendar.dns.DnsDirectCaller;
import org.kendar.dns.DnsRunnable;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class HttpDnsRunnable implements Callable<List<String>> {
    private final String requestedServer;
    private final String requestedDomain;
    private final LoggerBuilder loggerBuilder;

    public HttpDnsRunnable(String requestedServer, String requestedDomain, LoggerBuilder loggerBuilder) {
        this.requestedServer = requestedServer;
        this.requestedDomain = requestedDomain;
        this.loggerBuilder = loggerBuilder;
    }

    @Override
    public List<String> call() throws Exception {

        URL url = new URL("http://"+requestedServer+"/api/dns/lookup/"+requestedDomain);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        int status = con.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        /*var caller = new DnsDirectCaller(loggerBuilder);
        var result = caller.testDnsServer(requestedServer,requestedDomain);
        if(result == null || result.isEmpty()) return new ArrayList<>();*/
        var rs = new ArrayList<String>();
        var rres= content.toString();
        if(rres.length()>0){
            rs.add(rres);
        }
        //rs.add(result);
        return rs;
    }
}
