package org.kendar.http;
/*
 * 	Student:		Stefano Lupo
 *  Student No:		14334933
 *  Degree:			JS Computer Engineering
 *  Course: 		3D3 Computer Networks
 *  Date:			02/04/2017
 */

import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * The Proxy creates a Server Socket which will wait for connections on the specified port.
 * Once a connection arrives and a socket is accepted, the Proxy creates a RequestHandler object
 * on a new thread and passes the socket to it to be handled.
 * This allows the Proxy to continue accept further connections while others are being handled.
 * <p>
 * The Proxy class is also responsible for providing the dynamic management of the proxy through the console
 * and is run on a separate thread in order to not interrupt the acceptance of socket connections.
 * This allows the administrator to dynamically block web sites in real time.
 * <p>
 * The Proxy server is also responsible for maintaining cached copies of the any websites that are requested by
 * clients and this includes the HTML markup, images, css and js files associated with each webpage.
 * <p>
 * Upon closing the proxy server, the HashMaps which hold cached items and blocked sites are serialized and
 * written to a file and are loaded back in when the proxy is started once more, meaning that cached and blocked
 * sites are maintained.
 * <p>
 * export http_proxy="<a href="http://127.0.0.1:1081">...</a>"
 * ex<a href="port">https_proxy="htt</<a href="a>p://127.0.0.1:1081"
 ">* cur</a>l  "https://httpbin.org/anything"
 */
public class HttpsProxyImpl {


    private final boolean useCache;
    private final DnsMultiResolver resolver;
    private final boolean interceptAllHttp;
    private final Logger log;
    private ServerSocket serverSocket;
    /**
     * Semaphore for Proxy and Consolee Management System.
     */
    private volatile boolean running = true;


    /**
     * Create the Proxy Server
     *
     * @param port             Port number to run proxy server from.
     * @param interceptAllHttp
     */
    public HttpsProxyImpl(int port, boolean useCache, LoggerBuilder builder,
                          DnsMultiResolver resolver, boolean interceptAllHttp) {
        this.log = builder.build(HttpsProxy.class);
        this.useCache = useCache;
        this.resolver = resolver;
        this.interceptAllHttp = interceptAllHttp;

        try {
            // Create the Server Socket for the Proxy
            serverSocket = new ServerSocket(port);

            // Set the timeout
            //serverSocket.setSoTimeout(100000);	// debug
            log.debug("Waiting for client on port " + serverSocket.getLocalPort() + "..");
            running = true;
        }

        // Catch exceptions associated with opening socket
        catch (SocketException se) {
            log.warn("Socket Exception when connecting to client", se);
        } catch (SocketTimeoutException ste) {
            log.warn("Timeout occured while connecting to client", ste);
        } catch (IOException io) {
            log.warn("IO exception when connecting to client", io);
        }
    }

    private static final ExecutorService executorService = Executors.newFixedThreadPool(60);
    /**
     * Listens to port and accepts new socket connections.
     * Creates a new thread to handle the request and passes it the socket connection and continues listening.
     */
    public void listen() {

        while (running) {
            try {
                // serverSocket.accpet() Blocks until a connection is made
                Socket socket = serverSocket.accept();

                //executorService.submit(new Handler2(socket,resolver, interceptAllHttp));
                executorService.submit(new RequestHandler(socket, useCache, log, resolver,interceptAllHttp));
            } catch (SocketException e) {
                // Socket exception is triggered by management system to shut down the proxy
                log.debug("Server closed", e);
            } catch (IOException e) {
                log.debug("Server closed", e);
            }
        }
    }
}