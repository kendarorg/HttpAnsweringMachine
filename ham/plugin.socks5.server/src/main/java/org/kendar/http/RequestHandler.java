package org.kendar.http;

import org.kendar.servers.dns.DnsMultiResolver;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.Locale;

public class RequestHandler implements Runnable {

    public static final int READ_BUFFER_SIZE = 8192;
    /**
     * Socket connected to client passed by Proxy server
     */
    Socket clientSocket;
    private final boolean useCache;
    private final Logger log;
    private final DnsMultiResolver multiResolver;

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
     * @param clientSocket socket connected to the client
     * @param log
     */
    public RequestHandler(Socket clientSocket, boolean useCache, Logger log, DnsMultiResolver multiResolver) {
        this.clientSocket = clientSocket;
        this.useCache = useCache;
        this.log = log;
        this.multiResolver = multiResolver;
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

        String originalRequest = requestString;
        log.trace("Reuest Received " + requestString);
        // Get the Request type
        String request = requestString.substring(0, requestString.indexOf(' '));

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


        // Check if site is blocked
        if (HttpsProxyImpl.isBlocked(urlString)) {
            log.debug("Blocked site requested : " + urlString);
            blockedSiteRequested();
            return;
        }


        // Check request type
        if (request.equals("CONNECT")) {
            log.debug("HTTPS Request for : " + urlString + "\n");
            handleHTTPSRequest(urlString);
        } else {
            log.debug("HTTP Request for : " + urlString + "\n");
            try {
                handleHTTPRequest(urlString, originalRequest);
            } catch (URISyntaxException e) {
                log.error("Error syntax uri " + urlString, e);
            }
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
    }


    /**
     * Sends the specified cached file to the client
     *
     * @param cachedFile The file to be sent (can be image/text)
     */
    private void sendCachedPageToClient(File cachedFile) {
        // Read from File containing cached web page
        try {
            // If file is an image write data to client using buffered image.
            String fileExtension = cachedFile.getName().substring(cachedFile.getName().lastIndexOf('.'));

            // Response that will be sent to the server
            String response;
            if ((fileExtension.contains(".png")) || fileExtension.contains(".jpg") ||
                    fileExtension.contains(".jpeg") || fileExtension.contains(".gif")) {
                // Read in image from storage
                BufferedImage image = ImageIO.read(cachedFile);

                if (image == null) {
                    log.trace("Image " + cachedFile.getName() + " was null");
                    response = "HTTP/1.0 404 NOT FOUND \n" +
                            "Proxy-agent: ProxyServer/1.0\n" +
                            "\r\n";
                    proxyToClientBw.write(response);
                    proxyToClientBw.flush();
                } else {
                    response = "HTTP/1.0 200 OK\n" +
                            "Proxy-agent: ProxyServer/1.0\n" +
                            "\r\n";
                    proxyToClientBw.write(response);
                    proxyToClientBw.flush();
                    ImageIO.write(image, fileExtension.substring(1), clientSocket.getOutputStream());
                }
            }

            // Standard text based file requested
            else {
                BufferedReader cachedFileBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(cachedFile)));

                response = "HTTP/1.0 200 OK\n" +
                        "Proxy-agent: ProxyServer/1.0\n" +
                        "\r\n";
                proxyToClientBw.write(response);
                proxyToClientBw.flush();

                String line;
                while ((line = cachedFileBufferedReader.readLine()) != null) {
                    proxyToClientBw.write(line);
                }
                proxyToClientBw.flush();

                // Close resources
                if (cachedFileBufferedReader != null) {
                    cachedFileBufferedReader.close();
                }
            }


            // Close Down Resources
            if (proxyToClientBw != null) {
                proxyToClientBw.close();
            }

        } catch (IOException e) {
            log.error("Error Sending Cached file to client", e);
        }
    }


    /**
     * Sends the contents of the file specified by the urlString to the client
     *
     * @param urlString URL ofthe file requested
     */
    private void sendNonCachedToClient(String urlString) {

        try {

            // Compute a logical file name as per schema
            // This allows the files on stored on disk to resemble that of the URL it was taken from
            int fileExtensionIndex = urlString.lastIndexOf(".");
            String fileExtension;

            // Get the type of file
            fileExtension = urlString.substring(fileExtensionIndex, urlString.length());

            // Get the initial file name
            String fileName = urlString.substring(0, fileExtensionIndex);


            // Trim off http://www. as no need for it in file name
            fileName = fileName.substring(fileName.indexOf('.') + 1);

            // Remove any illegal characters from file name
            fileName = fileName.replace("/", "__");
            fileName = fileName.replace('.', '_');

            // Trailing / result in index.html of that directory being fetched
            if (fileExtension.contains("/")) {
                fileExtension = fileExtension.replace("/", "__");
                fileExtension = fileExtension.replace('.', '_');
                fileExtension += ".html";
            }

            fileName = fileName + fileExtension;


            // Attempt to create File to cache to
            boolean caching = useCache;
            File fileToCache = null;
            BufferedWriter fileToCacheBW = null;

            if (useCache) {
                try {
                    // Create File to cache
                    fileToCache = new File("cached/" + fileName);

                    if (!fileToCache.exists()) {
                        fileToCache.createNewFile();
                    }

                    // Create Buffered output stream to write to cached copy of file
                    fileToCacheBW = new BufferedWriter(new FileWriter(fileToCache));
                } catch (IOException e) {
                    log.debug("Couldn't cache: " + fileName, e);
                    caching = false;
                } catch (NullPointerException e) {
                    log.error("NPE opening file", e);
                }
            }

            // Check if file is an image
            if ((fileExtension.contains(".png")) || fileExtension.contains(".jpg") ||
                    fileExtension.contains(".jpeg") || fileExtension.contains(".gif")) {
                // Create the URL
                URL remoteURL = new URL(urlString);
                BufferedImage image = ImageIO.read(remoteURL);

                if (image != null) {
                    // Cache the image to disk
                    if (useCache) {
                        ImageIO.write(image, fileExtension.substring(1), fileToCache);
                    }

                    // Send response code to client
                    String line = "HTTP/1.0 200 OK\n" +
                            "Proxy-agent: ProxyServer/1.0\n" +
                            "\r\n";
                    proxyToClientBw.write(line);
                    proxyToClientBw.flush();

                    // Send them the image data
                    ImageIO.write(image, fileExtension.substring(1), clientSocket.getOutputStream());

                    // No image received from remote server
                } else {
                    log.debug("Sending 404 to client as image wasn't received from server"
                            + fileName);
                    String error = "HTTP/1.0 404 NOT FOUND\n" +
                            "Proxy-agent: ProxyServer/1.0\n" +
                            "\r\n";
                    proxyToClientBw.write(error);
                    proxyToClientBw.flush();
                    return;
                }
            }

            // File is a text file
            else {

                // Create the URL
                URL remoteURL = new URL(urlString);
                // Create a connection to remote server
                HttpURLConnection proxyToServerCon = (HttpURLConnection) remoteURL.openConnection();
                proxyToServerCon.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                proxyToServerCon.setRequestProperty("Content-Language", "en-US");
                proxyToServerCon.setUseCaches(false);
                proxyToServerCon.setDoOutput(true);

                // Create Buffered Reader from remote Server
                BufferedReader proxyToServerBR = new BufferedReader(new InputStreamReader(proxyToServerCon.getInputStream()));


                // Send success code to client
                String line = "HTTP/1.0 200 OK\n" +
                        "Proxy-agent: ProxyServer/1.0\n" +
                        "\r\n";
                proxyToClientBw.write(line);


                // Read from input stream between proxy and remote server
                while ((line = proxyToServerBR.readLine()) != null) {
                    // Send on data to client
                    proxyToClientBw.write(line);

                    // Write to our cached copy of the file
                    if (caching && useCache) {
                        fileToCacheBW.write(line);
                    }
                }

                // Ensure all data is sent by this point
                proxyToClientBw.flush();

                // Close Down Resources
                if (proxyToServerBR != null) {
                    proxyToServerBR.close();
                }
            }


            if (caching && useCache) {
                // Ensure data written and add to our cached hash maps
                fileToCacheBW.flush();
                HttpsProxyImpl.addCachedPage(urlString, fileToCache);
            }

            // Close down resources
            if (fileToCacheBW != null) {
                fileToCacheBW.close();
            }

            if (proxyToClientBw != null) {
                proxyToClientBw.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Handles HTTPS requests between client and remote server
     *
     * @param urlString desired file to be transmitted over https
     */
    private void handleHTTPSRequest(String urlString) {
        // Extract the URL and port of remote
        String url = urlString;//.substring(7);
        /*if(urlString.toLowerCase(Locale.ROOT).contains("://")){
            url = urlString.substring(7);
        }*/
        if (url.toLowerCase(Locale.ROOT).startsWith("https://")) {
            url = url.substring(8);
        }
        if (url.toLowerCase(Locale.ROOT).startsWith("http://")) {
            url = url.substring(7);
        }
        String pieces[] = url.split(":");
        url = pieces[0];
        int port = Integer.valueOf(pieces[1]);

        try {
            // Only first line of HTTPS request has been read at this point (CONNECT *)
            // Read (and throw away) the rest of the initial data on the stream
            //var lines = proxyToClientBr.lines().collect(Collectors.toList());
            /*for(int i=0;i<4;i++){
                var result = proxyToClientBr.readLine();
                //System.out.println(result);
            }*/

            // Get actual IP associated with this URL through DNS
            InetAddress address = InetAddress.getByName(multiResolver.resolve(url).get(0));

            // Open a socket to the remote server
            Socket proxyToServerSocket = new Socket(address, port);
            proxyToServerSocket.setSoTimeout(5000);

            // Send Connection established to the client
            String line = "HTTP/1.0 200 Connection established\r\n" +
                    "Proxy-Agent: ProxyServer/1.0\r\n" +
                    "\r\n";
            proxyToClientBw.write(line);
            proxyToClientBw.flush();


            // Client and Remote will both start sending data to proxy at this point
            // Proxy needs to asynchronously read data from each party and send it to the other party


            //Create a Buffered Writer betwen proxy and remote
            BufferedWriter proxyToServerBW = new BufferedWriter(new OutputStreamWriter(proxyToServerSocket.getOutputStream()));

            // Create Buffered Reader from proxy and remote
            BufferedReader proxyToServerBR = new BufferedReader(new InputStreamReader(proxyToServerSocket.getInputStream()));


            // Create a new thread to listen to client and transmit to server
            ClientToServerHttpsTransmit clientToServerHttps =
                    new ClientToServerHttpsTransmit(clientSocket.getInputStream(), proxyToServerSocket.getOutputStream());

            httpsClientToServer = new Thread(clientToServerHttps);
            httpsClientToServer.start();


            // Listen to remote server and relay to client
            try {
                byte[] buffer = new byte[READ_BUFFER_SIZE];
                int read;
                do {
                    read = proxyToServerSocket.getInputStream().read(buffer);
                    if (read > 0) {
                        clientSocket.getOutputStream().write(buffer, 0, read);
                        if (proxyToServerSocket.getInputStream().available() < 1) {
                            clientSocket.getOutputStream().flush();
                        }
                    }
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
        }
    }


    /**
     * Listen to data from client and transmits it to server.
     * This is done on a separate thread as must be done
     * asynchronously to reading data from server and transmitting
     * that data to the client.
     */
    class ClientToServerHttpsTransmit implements Runnable {

        InputStream proxyToClientIS;
        OutputStream proxyToServerOS;

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
                    }
                } while (read >= 0);
            } catch (SocketTimeoutException ste) {
                log.trace("Socket timeout", ste);
            } catch (IOException e) {
                log.trace("Proxy to client HTTPS read timed out", e);
            }
        }
    }


    /**
     * This method is called when user requests a page that is blocked by the proxy.
     * Sends an access forbidden message back to the client
     */
    private void blockedSiteRequested() {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            String line = "HTTP/1.0 403 Access Forbidden \n" +
                    "User-Agent: ProxyServer/1.0\n" +
                    "\r\n";
            bufferedWriter.write(line);
            bufferedWriter.flush();
        } catch (IOException e) {
            log.error("Error writing to client when requested a blocked site", e);
        }
    }


    private void invalidSiteRequested(String substring) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            bufferedWriter.write(substring);
            bufferedWriter.flush();
        } catch (IOException e) {
            log.error("Error writing to client when requested a blocked site");
        }
    }


    private void handleHTTPRequest(String origAddress, String originalRequest) throws URISyntaxException {
        // Extract the URL and port of remote

        var uri = new URI(origAddress);//.substring(7);
        /*if(urlString.toLowerCase(Locale.ROOT).contains("://") && !urlString.toLowerCase(Locale.ROOT).startsWith("http:")){
            url = urlString.substring(7);
        }*/
        var url = uri.getHost();
        int port = uri.getPort();
        if (port == -1) port = 80;

        try {
            // Only first line of HTTPS request has been read at this point (CONNECT *)
            // Read (and throw away) the rest of the initial data on the stream
            //var lines = proxyToClientBr.lines().collect(Collectors.toList());
            /*for(int i=0;i<4;i++){
                var result = proxyToClientBr.readLine();
                //System.out.println(result);
            }*/

            String line = originalRequest + "\r\n";
            /*proxyToClientBw.write(line);
            proxyToClientBw.flush();*/

            // Get actual IP associated with this URL through DNS
            InetAddress address = InetAddress.getByName(multiResolver.resolve(url).get(0));

            // Open a socket to the remote server
            Socket proxyToServerSocket = new Socket(address, port);
            if (address.isAnyLocalAddress() || address.isLoopbackAddress()) {
                proxyToServerSocket.setSoTimeout(100);
            } else {
                proxyToServerSocket.setSoTimeout(10000);
            }

            var writer = new OutputStreamWriter(proxyToServerSocket.getOutputStream());
            writer.write(line);
            char[] data = new char[1024];
            while (proxyToClientBr.ready()) {
                var dataLength = proxyToClientBr.read(data);
                if (dataLength <= 0) break;
                writer.write(data, 0, dataLength);
            }
            writer.flush();


            // Client and Remote will both start sending data to proxy at this point
            // Proxy needs to asynchronously read data from each party and send it to the other party


            //Create a Buffered Writer betwen proxy and remote
            /*BufferedWriter proxyToServerBW = new BufferedWriter(new OutputStreamWriter(proxyToServerSocket.getOutputStream()));

            // Create Buffered Reader from proxy and remote
            BufferedReader proxyToServerBR = new BufferedReader(new InputStreamReader(proxyToServerSocket.getInputStream()));

            proxyToServerBW.write(line);*/


            // Create a new thread to listen to client and transmit to server
            /*ClientToServerHttpsTransmit clientToServerHttps =
                    new ClientToServerHttpsTransmit(clientSocket.getInputStream(), proxyToServerSocket.getOutputStream());

            httpsClientToServer = new Thread(clientToServerHttps);
            httpsClientToServer.start();*/


            // Listen to remote server and relay to client
            try {
                byte[] buffer = new byte[4096];
                int read;
                do {
                    read = proxyToServerSocket.getInputStream().read(buffer);
                    if (read > 0) {
                        clientSocket.getOutputStream().write(buffer, 0, read);
                        if (proxyToServerSocket.getInputStream().available() < 1) {
                            clientSocket.getOutputStream().flush();
                        }
                    }
                } while (read >= 0);
            } catch (SocketTimeoutException e) {

            } catch (IOException e) {
                e.printStackTrace();
            }


            // Close Down Resources
            if (proxyToServerSocket != null) {
                proxyToServerSocket.close();
            }

            /*if(proxyToServerBR != null){
                proxyToServerBR.close();
            }

            if(proxyToServerBW != null){
                proxyToServerBW.close();
            }*/

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
            log.error("Error on HTTP : " + origAddress, e);
        }
    }
}



