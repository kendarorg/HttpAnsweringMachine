package org.kendar.http;

import org.apache.http.conn.util.InetAddressUtils;
import org.kendar.servers.dns.DnsMultiResolver;
import org.slf4j.Logger;

import javax.annotation.RegEx;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.Locale;
import java.util.regex.Pattern;

@SuppressWarnings("HttpUrlsUsage")
public class RequestHandler implements Runnable {

    public static final int READ_BUFFER_SIZE = 16000;
    private final boolean useCache;
    private final Logger log;
    private final DnsMultiResolver multiResolver;
    private final boolean interceptAllHttp;
    /**
     * Socket connected to client passed by Proxy server
     */
    final Socket clientSocket;
    /**
     * Read data client sends to proxy
     */
    BufferedReader proxyToClientBr;

    /**
     * Send data from proxy to client
     */
    BufferedWriter proxyToClientBw;


    /**
     * Thread that is used to transmit data read from client to server when using HTTPS
     * Reference to this is required so it can be closed once completed.
     */
    private Thread httpsClientToServer;


    /**
     * Creates a ReuqestHandler object capable of servicing HTTP(S) GET requests
     *
     * @param clientSocket     socket connected to the client
     * @param log
     * @param interceptAllHttp
     */
    public RequestHandler(Socket clientSocket, boolean useCache, Logger log, DnsMultiResolver multiResolver, boolean interceptAllHttp) {
        this.clientSocket = clientSocket;
        this.useCache = useCache;
        this.log = log;
        this.multiResolver = multiResolver;
        this.interceptAllHttp = interceptAllHttp;
        try {
            this.clientSocket.setSoTimeout(2000);
            proxyToClientBr = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            proxyToClientBw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Reads and examines the requestString and calls the appropriate method based
     * on the request type.
     */
    @Override
    public void run() {

        // Get Request from client
        String requestString;
        try {
            requestString = proxyToClientBr.readLine();
            if (requestString == null || requestString.isEmpty()) {
                return;
            }
        } catch (IOException e) {
            log.trace("Error reading request from client", e);
            return;
        }

        // Parse out URL
        //POST http://www.test.com/fuffa

        String originalRequest = requestString;
        log.trace("Reuest Received " + requestString);
        // Get the Request type
        String requestVerb = requestString.substring(0, requestString.indexOf(' '));

        // remove request type and space
        String urlString = requestString.substring(requestString.indexOf(' ') + 1);

        // Remove everything past next space
        urlString = urlString.substring(0, urlString.indexOf(' '));

        /*if(urlString.length()<4){
            invalidSiteRequested(requestString.substring(requestString.indexOf(' ')+1));
            return;
        }*/
        // Prepend http:// if necessary to create correct URL
        if (urlString.length() < 4 || !urlString.substring(0, 4).equals("http")) {
            String temp = "http:/";
            if (!urlString.startsWith("/")) {
                temp += "/";
            }
            urlString = temp + urlString;
        }

        // Check request type
        if (requestVerb.equals("CONNECT")) {
            log.debug("HTTPS Request for : " + urlString + "\n");

            System.out.println("HTTPS Request for : " + urlString + "\n");
            handleHTTPSRequest(urlString,originalRequest,true);
        } else {
            System.out.println("HTTP Request for : " + urlString + "\n");
            log.debug("HTTP Request for : " + urlString + "\n");

                handleHTTPSRequest(urlString,originalRequest,false);

            // Check if we have a cached copy
            /*File file;
            if(useCache && (file = HttpsProxyImpl.getCachedPage(urlString)) != null){
                log.trace("Cached Copy found for : " + urlString + "\n");
                sendCachedPageToClient(file);
            } else {
                log.trace("HTTP GET for : " + urlString + "\n");
                sendNonCachedToClient(urlString);
            }*/
        }
        System.out.println("END "+urlString);
    }

    private static Pattern digits = Pattern.compile("\\d+");

    /**
     * Handles HTTPS requests between client and remote server
     *
     * @param urlString desired file to be transmitted over https
     */
    private void handleHTTPSRequest(String urlString, String originalRequest, boolean isHttps) {
        // Extract the URL and port of remote
        //String url = urlString;//.substring(7);
        /*if(urlString.toLowerCase(Locale.ROOT).contains("://")){
            url = urlString.substring(7);
        }*/
        URI uri;

        try {
            uri = new URI(urlString);//.substring(7);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        /*if(urlString.toLowerCase(Locale.ROOT).contains("://") && !urlString.toLowerCase(Locale.ROOT).startsWith("http:")){
            url = urlString.substring(7);
        }*/
        var url = uri.getHost();
        int port = uri.getPort();
        if (port == -1){
            port = isHttps?443:80;
        }

        try {
            // Only first line of HTTPS request has been read at this point (CONNECT *)
            // Read (and throw away) the rest of the initial data on the stream
            //var lines = proxyToClientBr.lines().collect(Collectors.toList());
            /*for(int i=0;i<4;i++){
                var result = proxyToClientBr.readLine();
                //System.out.println(result);
            }*/

            var resolved = multiResolver.resolve(url);
            if(resolved.isEmpty()){
                proxyToClientBw.close();
                return;
            }
            // Get actual IP associated with this URL through DNS
            InetAddress address = InetAddress.getByName(resolved.get(0));

            // Open a socket to the remote server
            Socket proxyToServerSocket = new Socket(address, port);
            proxyToServerSocket.setSoTimeout(5000);

            // Send Connection established to the client
            String line = "HTTP/1.0 200 Connection established\r\n" +
                    "Proxy-Agent: ProxyServer/1.0\r\n" +
                    "\r\n";

            if(isHttps) {

                proxyToClientBw.write(line);
                proxyToClientBw.flush();
            }

            // Client and Remote will both start sending data to proxy at this point
            // Proxy needs to asynchronously read data from each party and send it to the other party


            //Create a Buffered Writer betwen proxy and remote
            BufferedWriter proxyToServerBW = new BufferedWriter(new OutputStreamWriter(proxyToServerSocket.getOutputStream()));

            // Create Buffered Reader from proxy and remote
            BufferedReader proxyToServerBR = new BufferedReader(new InputStreamReader(proxyToServerSocket.getInputStream()));

            if(!isHttps){
                proxyToServerBW.write(originalRequest+"\r\n");
                proxyToServerBW.flush();
            }


            // Create a new thread to listen to client and transmit to server
//            ClientToServerHttpsTransmit clientToServerHttps =
//                    new ClientToServerHttpsTransmit(clientSocket.getInputStream(), proxyToServerSocket.getOutputStream());

            try {
                // Read byte by byte from client and send directly to server
                byte[] buffer = new byte[4096];
                int read;
                do {
                    if(clientSocket.getInputStream().available()>0) {
                        read = clientSocket.getInputStream().read(buffer);
                        if (read > 0) {
                            proxyToServerSocket.getOutputStream().write(buffer, 0, read);
                            //if (proxyToClientIS.available() < 1) {
                            //proxyToServerOS.flush();
                            //}
                            System.out.println("Comm " + read);
                        }
                    }else{
                        break;
                    }
                } while (read >= 0);
                proxyToServerSocket.getOutputStream().flush();
            } catch (SocketTimeoutException ste) {
                log.trace("Socket timeout", ste);
            } catch (IOException e) {
                log.trace("Proxy to client HTTPS read timed out", e);
            }
//            httpsClientToServer = new Thread(clientToServerHttps);
//            httpsClientToServer.start();

            //clientToServerHttps.run();


            // Listen to remote server and relay to client
            try {
                byte[] buffer = new byte[READ_BUFFER_SIZE];
                int read;
                proxyToServerSocket.setSoTimeout(1000);
                do {
                    //if(proxyToServerSocket.getInputStream().available()>0) {
                        read = proxyToServerSocket.getInputStream().read(buffer);
                        if (read > 0) {
                            clientSocket.getOutputStream().write(buffer, 0, read);
                            //if (proxyToServerSocket.getInputStream().available() < 1) {
                            clientSocket.getOutputStream().flush();
                            //}
                            System.out.println("WRITTEN " + read);
                        }
//                    }else {
//                        break;
//                    }
                } while (read >= 0);
            } catch (SocketTimeoutException e) {
                log.trace("Timeout reading socket", e);
            } catch (IOException e) {
                log.trace("Error reading socket", e);
            }


            // Close Down Resources
            if (proxyToServerSocket != null) {
                proxyToServerSocket.close();
            }

            if (proxyToServerBR != null) {
                proxyToServerBR.close();
            }

            if (proxyToServerBW != null) {
                proxyToServerBW.close();
            }

            if (proxyToClientBw != null) {
                proxyToClientBw.close();
            }


        } catch (SocketTimeoutException e) {
            String line = "HTTP/1.0 504 Timeout Occured after 10s\n" +
                    "User-Agent: ProxyServer/1.0\n" +
                    "\r\n";
            try {
                proxyToClientBw.write(line);
                proxyToClientBw.flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } catch (Exception e) {
            log.error("Error on HTTPS : " + urlString, e);
        }finally {
            try{
                clientSocket.close();
            }catch (Exception ex){
            }
        }
    }



    /**
     * Listen to data from client and transmits it to server.
     * This is done on a separate thread as must be done
     * asynchronously to reading data from server and transmitting
     * that data to the client.
     */
    class ClientToServerHttpsTransmit implements Runnable {

        final InputStream proxyToClientIS;
        final OutputStream proxyToServerOS;

        /**
         * Creates Object to Listen to Client and Transmit that data to the server
         *
         * @param proxyToClientIS Stream that proxy uses to receive data from client
         * @param proxyToServerOS Stream that proxy uses to transmit data to remote server
         */
        public ClientToServerHttpsTransmit(InputStream proxyToClientIS, OutputStream proxyToServerOS) {
            this.proxyToClientIS = proxyToClientIS;
            this.proxyToServerOS = proxyToServerOS;
        }

        @Override
        public void run() {
            try {
                // Read byte by byte from client and send directly to server
                byte[] buffer = new byte[4096];
                int read;
                do {
                    read = proxyToClientIS.read(buffer);
                    if (read > 0) {
                        proxyToServerOS.write(buffer, 0, read);
                        if (proxyToClientIS.available() < 1) {
                            proxyToServerOS.flush();
                        }
                        System.out.println("Comm "+read);
                    }
                } while (read >= 0);
            } catch (SocketTimeoutException ste) {
                log.trace("Socket timeout", ste);
            } catch (IOException e) {
                log.trace("Proxy to client HTTPS read timed out", e);
            }
        }
    }
}



