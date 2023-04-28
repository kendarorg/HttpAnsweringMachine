package org.kendar.globaltest;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;

public class HttpChecker {
    int seconds;
    int proxyPort = -1;
    String proxyUrl = null;
    String url;
    private Runnable onError = null;
    private boolean showError = true;

    protected HttpChecker() {

    }

    public static HttpChecker checkForSite(int seconds, String url) {
        var result = new HttpChecker();
        result.seconds = seconds;
        result.url = url;
        return result;
    }

    public HttpChecker noError() {
        this.showError = false;
        return this;
    }

    public HttpChecker withProxy(String proxyUrl, int proxyPort) {
        this.proxyUrl = proxyUrl;
        this.proxyPort = proxyPort;
        return this;
    }

    public HttpChecker onError(Runnable onError) {
        this.onError = onError;
        return this;
    }

    public boolean run() throws Exception {
        var now = System.currentTimeMillis();
        var end = now + seconds * 1000L;
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
            Sleeper.sleep(1000);
        }
        if (showError) {
            LogWriter.errror("testing " + url);
        }
        if (onError != null) {
            onError.run();
        }
        return false;
    }
}
