package org.kendar.ham;

import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;

import java.util.List;

/**
 * Basic builder functions, exposed for general usage
 */
public interface HamBasicBuilder  {
    /**
     * The ham API port (default 80)
     * @param port
     * @return
     */
    HamBasicBuilder withPort(int port);


    /**
     * The dns to use for the requests
     * @param ip
     * @param port default 53
     * @return
     */
    HamBasicBuilder withDns(String ip,int port);

    /**
     * The socks proxy to use for connections. Usually the internal HAM proxy
     * @param ip
     * @param port defaults to 1080
     * @return
     */
    HamBasicBuilder withSocksProxy(String ip,int port);

    /**
     * The dns to use for the requests, default port 53
     * @param ip
     * @return
     */
    HamBasicBuilder withDns(String ip);

    /**
     * Use https (and 443) as default protocol
     * @return
     */
    HamBasicBuilder withHttps();

    /**
     * Start the DNS calls builder
     * @return
     */
    DnsBuilder dns();

    /**
     * Start the certificates calls builder
     * @return
     */
    CertificatesBuilder certificates();

    /**
     * Start the proxies calls builder
     * @return
     */
    ProxyBuilder proxies();

    /**
     * Start the settings calls builder
     * @return
     */
    SettingsBuilder settings();



    /**
     * Make a call to HAM remote invocation and return a parsed JSON object
     * @param request
     * @param clazz
     * @return
     * @param <T>
     * @throws HamException
     */
    <T> T callJson(Request request, Class<T> clazz) throws HamException;

    /**
     * Make a call to HAM remote invocation and return a parsed List of JSON object
     * @param request
     * @param clazz
     * @return
     * @param <T>
     * @throws HamException
     */
    <T> List<T> callJsonList(Request request, Class<T> clazz) throws HamException;


    /**
     * Execute a call through HAM remote invocation
     * @param request
     * @return
     * @throws HamException
     */
    Response call(Request request) throws HamException;

    <T> T pluginBuilder(Class<T> clazz);
}
