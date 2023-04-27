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
 * export http_proxy="http://127.0.0.1:1081"
 * export https_proxy="http://127.0.0.1:1081"
 * curl  "https://httpbin.org/anything"
 */
public class HttpsProxyImpl implements Runnable {


    /**
     * Data structure for constant order lookup of cache items.
     * Key: URL of page/image requested.
     * Value: File in storage associated with this key.
     */
    static HashMap<String, File> cache;
    /**
     * Data structure for constant order lookup of blocked sites.
     * Key: URL of page/image requested.
     * Value: URL of page/image requested.
     */
    static HashMap<String, String> blockedSites;
    /**
     * ArrayList of threads that are currently running and servicing requests.
     * This list is required in order to join all threads on closing of server
     */
    static ArrayList<Thread> servicingThreads;

    // Main method for the program
    /*public static void main(String[] args) {
        // Create an instance of Proxy and begin listening for connections
        HttpsProxyImpl myProxy = new HttpsProxyImpl(8085,true);
        myProxy.listen();
    }*/
    private final boolean useCache;
    private final DnsMultiResolver resolver;
    private final Logger log;
    private ServerSocket serverSocket;
    /**
     * Semaphore for Proxy and Consolee Management System.
     */
    private volatile boolean running = true;


    /**
     * Create the Proxy Server
     *
     * @param port Port number to run proxy server from.
     */
    public HttpsProxyImpl(int port, boolean useCache, LoggerBuilder builder, DnsMultiResolver resolver) {
        this.log = builder.build(HttpsProxy.class);
        this.useCache = useCache;
        this.resolver = resolver;
        // Load in hash map containing previously cached sites and blocked Sites
        cache = new HashMap<>();
        blockedSites = new HashMap<>();

        // Create array list to hold servicing threads
        servicingThreads = new ArrayList<>();

        setupCacheAndBlocks();

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

    /**
     * Looks for File in cache
     *
     * @param url of requested file
     * @return File if file is cached, null otherwise
     */
    public static File getCachedPage(String url) {
        return cache.get(url);
    }

    /**
     * Adds a new page to the cache
     *
     * @param urlString   URL of webpage to cache
     * @param fileToCache File Object pointing to File put in cache
     */
    public static void addCachedPage(String urlString, File fileToCache) {
        cache.put(urlString, fileToCache);
    }

    /**
     * Check if a URL is blocked by the proxy
     *
     * @param url URL to check
     * @return true if URL is blocked, false otherwise
     */
    public static boolean isBlocked(String url) {
        if (blockedSites.get(url) != null) {
            return true;
        } else {
            return false;
        }
    }

    private void setupCacheAndBlocks() {
        if (!useCache) return;
        try {
            // Load in cached sites from file
            File cachedSites = new File("cachedSites.txt");
            if (!cachedSites.exists()) {
                log.trace("No cached sites found - creating new file");
                cachedSites.createNewFile();
            } else {
                FileInputStream fileInputStream = new FileInputStream(cachedSites);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                cache = (HashMap<String, File>) objectInputStream.readObject();
                fileInputStream.close();
                objectInputStream.close();
            }

            // Load in blocked sites from file
            File blockedSitesTxtFile = new File("blockedSites.txt");
            if (!blockedSitesTxtFile.exists()) {
                log.trace("No blocked sites found - creating new file");
                blockedSitesTxtFile.createNewFile();
            } else {
                FileInputStream fileInputStream = new FileInputStream(blockedSitesTxtFile);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                blockedSites = (HashMap<String, String>) objectInputStream.readObject();
                fileInputStream.close();
                objectInputStream.close();
            }
        } catch (IOException e) {
            log.debug("Error loading previously cached sites file", e);
        } catch (ClassNotFoundException e) {
            log.debug("Class not found loading in preivously cached sites file", e);
        }
    }

    /**
     * Listens to port and accepts new socket connections.
     * Creates a new thread to handle the request and passes it the socket connection and continues listening.
     */
    public void listen() {

        while (running) {
            try {
                // serverSocket.accpet() Blocks until a connection is made
                Socket socket = serverSocket.accept();

                // Create new Thread and pass it Runnable RequestHandler
                Thread thread = new Thread(new RequestHandler(socket, useCache, log, resolver));

                // Key a reference to each thread so they can be joined later if necessary
                servicingThreads.add(thread);

                thread.start();
            } catch (SocketException e) {
                // Socket exception is triggered by management system to shut down the proxy
                log.debug("Server closed", e);
            } catch (IOException e) {
                log.debug("Server closed", e);
            }
        }
    }

    /**
     * Saves the blocked and cached sites to a file so they can be re loaded at a later time.
     * Also joins all of the RequestHandler threads currently servicing requests.
     */
    private void closeServer() {
        log.debug("Closing Server..");
        running = false;
        closeCacheAndBlocked();

        // Close Server Socket
        try {
            log.debug("Terminating Connection");
            serverSocket.close();
        } catch (Exception e) {
            log.error("Exception closing proxy's server socket", e);
        }

    }

    private void closeCacheAndBlocked() {
        if (!useCache) return;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("cachedSites.txt");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(cache);
            objectOutputStream.close();
            fileOutputStream.close();
            log.trace("Cached Sites written");

            FileOutputStream fileOutputStream2 = new FileOutputStream("blockedSites.txt");
            ObjectOutputStream objectOutputStream2 = new ObjectOutputStream(fileOutputStream2);
            objectOutputStream2.writeObject(blockedSites);
            objectOutputStream2.close();
            fileOutputStream2.close();
            log.trace("Blocked Site list saved");
            try {
                // Close all servicing threads
                for (Thread thread : servicingThreads) {
                    if (thread.isAlive()) {
                        log.trace("Waiting on " + thread.getId() + " to close..");
                        thread.join();
                        log.trace(" closed");
                    }
                }
            } catch (InterruptedException e) {
                log.trace("Interrupted service task", e);
            }

        } catch (IOException e) {
            log.debug("Error saving cache/blocked sites", e);
        }
    }

    /**
     * Creates a management interface which can dynamically update the proxy configurations
     * blocked : Lists currently blocked sites
     * cached	: Lists currently cached sites
     * close	: Closes the proxy server
     * *		: Adds * to the list of blocked sites
     */
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        String command;
        while (running) {
            //l("Enter new site to block, or type \"blocked\" to see blocked sites, \"cached\" to see cached sites, or \"close\" to close server.");
            command = scanner.nextLine();
            if (command.toLowerCase().equals("blocked")) {
                log.debug("Currently Blocked Sites");
                for (String key : blockedSites.keySet()) {
                    log.debug(key);
                }
            } else if (command.toLowerCase().equals("cached")) {
                log.debug("\nCurrently Cached Sites");
                for (String key : cache.keySet()) {
                    log.debug(key);
                }
            } else if (command.equals("close")) {
                running = false;
                closeServer();
            } else {
                blockedSites.put(command, command);
                log.debug(command + " blocked successfully");
            }
        }
        scanner.close();
    }

}