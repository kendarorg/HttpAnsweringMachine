package org.kendar.socks5;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created for http://stackoverflow.com/q/16351413/1266906.
 */
public class HttpProxyServer extends Thread {
    ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(9999)) {
            Socket socket;
            try {
                while ((socket = serverSocket.accept()) != null) {
                    executor.submit(new HttpProxyHandler(socket,executor));
                }
            } catch (IOException e) {
                e.printStackTrace();  // TODO: implement catch
            }
        } catch (IOException e) {
            e.printStackTrace();  // TODO: implement catch
            return;
        }
    }

}
