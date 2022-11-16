package org.kendar.ham;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.kendar.servers.http.RequestUtils;
import org.kendar.utils.Sleeper;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;

public class LocalHttpServer {
    public static class SimpleRequest{
        public byte[] data;
        public String path;
        public String method;
        public Map<String,String> headers;
        public Map<String, String> query;

        public String getStringData(){ return new String(data);}
        public HttpExchange httpExchange;
    }

    public static class LocalHandler{
        public  String path;
        public Consumer<SimpleRequest> consumer;
        private LocalHandler(){}

        public LocalHandler(String path, Consumer<SimpleRequest> consumer){

            this.path = path;
            this.consumer = consumer;
        }

    }

    public static HttpServer startServer(int port, LocalHandler ... handlers) throws HamTestException {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            for(var handler :handlers) {
                server.createContext(handler.path, exchange -> {
                    var sr = new SimpleRequest();
                    sr.httpExchange = exchange;
                    sr.path = exchange.getRequestURI().toString();
                    sr.headers = RequestUtils.headersToMap(exchange.getRequestHeaders());
                    sr.query = RequestUtils.queryToMap(exchange.getRequestURI().getRawQuery());
                    handler.consumer.accept(sr);
                });
            }
            server.setExecutor(null); // creates a default executor
            server.start();
            Sleeper.sleep(2000);
            return server;
        }catch(Exception ex){
            throw new HamTestException(ex);
        }
    }


    public static void sendResponse(HttpExchange exchange, int code, String data, Map<String,String> headers ) throws HamTestException {
        sendResponse(exchange,code,data.getBytes(StandardCharsets.UTF_8),headers);
    }
    public static void sendResponse(HttpExchange exchange, int code, byte[] data, Map<String,String> headers ) throws HamTestException {

        try {
            if (headers != null) {
                for (var header : headers.entrySet()) {
                    exchange.getResponseHeaders().set(header.getKey(), header.getValue());
                }
            }
            var dl = (data != null) ? (data.length) : -1;
            exchange.sendResponseHeaders(code, dl);


            if (data != null && dl>0) {
                OutputStream os = exchange.getResponseBody();
                os.write(data);
                os.flush();
                os.close();
            }
            exchange.close();
        }catch(Exception ex){
            throw new HamTestException(ex);
        }
    }
}
