package org.kendar.globaltest;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;

public class HttpChecker {
    public boolean checkForSite(int seconds, String url) throws Exception {

        return checkForSite(seconds, url, null, -1);
    }

    public boolean checkForSite(int seconds, String url, String proxyUrl, int proxyPort) throws Exception {
        var now = System.currentTimeMillis();
        var end = now + seconds * 1000;
        LogWriter.info("Testing for %d seconds %s: ", seconds, url);
        while (end > System.currentTimeMillis()) {
            System.out.print(".");
            if (proxyUrl != null) {
                var proxy = new HttpHost(proxyUrl, proxyPort, "http");
                var routePlanner = new DefaultProxyRoutePlanner(proxy);
                try (var httpclient = HttpClients.custom().setRoutePlanner(routePlanner).build()) {
                    var httpget = new HttpGet(url);
                    var httpresponse = httpclient.execute(httpget);
                    if (httpresponse.getStatusLine().getStatusCode() == 200) {
                        System.out.print("OK\n");
                        return true;
                    }
                } catch (Exception ex) {
                    //NOP
                }
            } else {

                try (var httpclient = HttpClients.createDefault()) {
                    var httpget = new HttpGet(url);
                    var httpresponse = httpclient.execute(httpget);
                    if (httpresponse.getStatusLine().getStatusCode() == 200) {
                        System.out.print("OK\n");
                        return true;
                    }
                } catch (Exception ex) {
                    //NOP
                }
            }
            Thread.sleep(1000);
        }
        LogWriter.errror("testing " + url);
        return false;
    }
}
