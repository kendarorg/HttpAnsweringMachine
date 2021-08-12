package org.kendar.servers.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsExchange;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.message.BasicNameValuePair;
import org.kendar.http.FilteringClassesHandler;
import org.kendar.http.HttpFilterType;
import org.kendar.servers.AnsweringHttpsServer;
import org.kendar.servers.dns.DnsMultiResolver;
import org.kendar.servers.proxy.SimpleProxyHandler;
import org.kendar.utils.LoggerBuilder;
import org.kendar.utils.MimeChecker;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.joining;

@Component
public class AnsweringHandlerImpl implements AnsweringHandler {
    public static final String MIRROR_REQUEST_HEADER = "X-MIRROR-REQUEST";
    public static final String TEST_EXPECT_100 = "X-TEST-EXPECT-100";
    public static final String TEST_OVERWRITE_HOST = "X-TEST-OVERWRITE-HOST";
    public static final String BLOCK_RECURSION = "X-BLOCK-RECURSIVE";
    private Logger logger;
    private SystemDefaultDnsResolver dnsResolver;
    private DnsMultiResolver multiResolver;
    private FilteringClassesHandler filteringClassesHandler;
    private SimpleProxyHandler simpleProxyHandler;
    private PoolingHttpClientConnectionManager connManager;
    private ObjectMapper mapper = new ObjectMapper();
    @Value("${https.forwardProtocol:https}")
    private String httpsForwardProtocol="https";
    @Value("${http.forwardProtocol:http}")
    private String httpForwardProtocol="http";
    @Value("${https.forwardPort:-1}")
    private int httpsForwardPort=-1;
    @Value("${http.forwardPort:-1}")
    private int httpForwardPort=-1;

    @Value("${dns.logging.query:false}")
    private boolean dnsLogginQuery;

    private ConcurrentHashMap<String,List<String>> domains = new ConcurrentHashMap<>();

    public AnsweringHandlerImpl(LoggerBuilder loggerBuilder, DnsMultiResolver multiResolver,
                                FilteringClassesHandler filteringClassesHandler,
                                SimpleProxyHandler simpleProxyHandler){
        this.logger = loggerBuilder.build(AnsweringHttpsServer.class);
        this.multiResolver = multiResolver;
        this.filteringClassesHandler = filteringClassesHandler;
        this.simpleProxyHandler = simpleProxyHandler;

    }

    @PostConstruct
    public void init(){
        this.dnsResolver = new SystemDefaultDnsResolver() {
            @Override
            public InetAddress[] resolve(final String host) throws UnknownHostException {
                List<String> hosts;
                if(domains.containsKey(host)){
                    hosts = domains.get(host);
                }else {
                    hosts = multiResolver.resolveRemote(host);
                }
                var address = new InetAddress[hosts.size()];
                for(int i=0;i< hosts.size();i++){
                    address[i] = InetAddress.getByName(hosts.get(i));
                }
                return address;
            }
        };
        this.connManager = new PoolingHttpClientConnectionManager(
                // We're forced to create a SocketFactory Registry.  Passing null
                //   doesn't force a default Registry, so we re-invent the wheel.
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", SSLConnectionSocketFactory.getSocketFactory())
                        .build(),
                dnsResolver  // Our DnsResolver
        );
        this.connManager.setMaxTotal(100);
    }

    private void mirrrorRequest(Request request, HttpExchange httpExchange){
        try {
            String myObjectInJson = mapper.writeValueAsString(request);
            httpExchange.getResponseHeaders().put("Access-Control-Allow-Origin",Collections.singletonList( "*"));
            httpExchange.sendResponseHeaders(200, myObjectInJson.getBytes().length);

            OutputStream os = httpExchange.getResponseBody();
            os.write(myObjectInJson.getBytes());
            os.close();
            httpExchange.close();
        }catch(Exception ex){
            logger.error("Error mirroring request ",ex);
        }
    }

    private boolean testExpect100(Request request, HttpExchange httpExchange) {
        String expect100 = request.getHeader(TEST_EXPECT_100);
        if(expect100!=null){
            mirrrorRequest(request,httpExchange);
            return true;
        }
        return false;
    }

    private void handleOverwriteHost(Request request, HttpExchange httpExchange) {
        String overwriteHost = request.getHeader(TEST_OVERWRITE_HOST);
        if(overwriteHost!=null){
            try {
                URL url = new URL(overwriteHost);
                request.setProtocol(url.getProtocol().toLowerCase(Locale.ROOT));
                request.setHost(url.getHost());
                if(url.getPort()==-1 && request.getProtocol().equalsIgnoreCase("https")){
                    request.setPort(443);
                }else if(url.getPort()==-1 && request.getProtocol().equalsIgnoreCase("http")){
                    request.setPort(80);
                }else if(url.getPort()>0) {
                    request.setPort(url.getPort());
                }
            } catch (MalformedURLException e) {
                //e.printStackTrace();
            }
        }
    }

    private boolean blockRecursive(Request request, HttpExchange httpExchange) throws IOException {
        String isRecursive = request.getHeader(BLOCK_RECURSION);
        if(isRecursive==null){
            return false;
        }
        String fullAddress = buildFullAddress(request);

        if(fullAddress.equalsIgnoreCase(isRecursive)){
            var error= "Recursive call to "+isRecursive;
            var data = error.getBytes();
            httpExchange.sendResponseHeaders(500, data.length);
            OutputStream os = httpExchange.getResponseBody();
            os.write(data);
            os.close();
            return true;
        }
        return false;
    }

    public boolean mirrorData(Request request, HttpExchange httpExchange){
        String isMirror = request.getHeader(MIRROR_REQUEST_HEADER);
        if(isMirror!=null){
            mirrrorRequest(request,httpExchange);
            return true;
        }
        return false;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        var requestUri = httpExchange.getRequestURI();
        var host = httpExchange.getRequestHeaders().getFirst("Host");
        logger.info(host+requestUri.toString());

        Request request = null;
        Response response = new Response();
        try {
            if(httpExchange instanceof HttpsExchange){
                request = Request.fromExchange(httpExchange,httpsForwardProtocol,httpsForwardPort);
            }else{
                request = Request.fromExchange(httpExchange,httpForwardProtocol,httpForwardPort);
            }

            handleOverwriteHost(request, httpExchange);

            if (handleSpecialRequests(httpExchange, request)) {
                return;
            }

            if(filteringClassesHandler.handle(HttpFilterType.PRE_RENDER,request,response,connManager)){
                sendResponse(response,httpExchange);
                return;
            }

            if(filteringClassesHandler.handle(HttpFilterType.API,request,response,connManager)){
                //ALWAYS WHEN CALLED
                sendResponse(response,httpExchange);
                return;
            }

            if(filteringClassesHandler.handle(HttpFilterType.STATIC,request,response,connManager)){
                //ALWAYS WHEN CALLED
                sendResponse(response,httpExchange);
                return;
            }

            request = simpleProxyHandler.translate(request);

            if(filteringClassesHandler.handle(HttpFilterType.PRE_CALL,request,response,connManager)){
                sendResponse(response,httpExchange);
                return;
            }

            callExternalSite(httpExchange, request, response);

            if(filteringClassesHandler.handle(HttpFilterType.POST_CALL,request,response,connManager)){
                sendResponse(response,httpExchange);
                return;
            }

            sendResponse(response,httpExchange);


        }catch(Exception ex){
            try {
                logger.error("ERROR HANDLING HTTP REQUEST ", ex);
                response.addHeader("X-Exception-Type", ex.getClass().getName());
                response.addHeader("X-Exception-Message", ex.getMessage());
                response.addHeader("X-Exception-PrevStatusCode", Integer.toString(response.getStatusCode()));
                response.setStatusCode(500);
                sendResponse(response, httpExchange);
            }catch (Exception xx){

            }
        }finally {
            try {
                filteringClassesHandler.handle(HttpFilterType.POST_RENDER,request,response,connManager);
            } catch (Exception e) {
                logger.error("ERROR CALLING POST RENDER ",e);
            }
        }
    }

    private boolean handleSpecialRequests(HttpExchange httpExchange, Request request) throws IOException {
        if(mirrorData(request, httpExchange)){
            return true;
        }
        if(testExpect100(request, httpExchange)){
            return true;
        }
        if(blockRecursive(request, httpExchange)){
            return true;
        }
        return false;
    }

    private static HttpRequestRetryHandler requestRetryHandler = (exception, executionCount, context) -> {
        if (executionCount == 1) {
            return false;
        } else {
            return true;
        }
    };

    private void callExternalSite(HttpExchange httpExchange, Request request, Response response) throws Exception {

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setRetryHandler(requestRetryHandler)
                .setConnectionManager(connManager).disableRedirectHandling().build();

        HttpRequestBase fullRequest = null;
        try {
            String fullAddress = buildFullAddress(request);
            fullRequest = createFullRequest(request, fullAddress);
            fullRequest.addHeader(BLOCK_RECURSION, fullAddress);
            for (var header : request.getHeaders().entrySet()) {
                if (!header.getKey().equalsIgnoreCase("host") &&
                        !header.getKey().equalsIgnoreCase("content-length")) {
                    fullRequest.addHeader(header.getKey(), header.getValue());
                }
            }
            if (request.isSoapRequest()) {
                HttpEntity entity = handleSoapRequest(request, httpExchange);
                ((HttpEntityEnclosingRequestBase) fullRequest).setEntity(entity);
            } else if (request.getPostParameters().size() > 0) {
                List<NameValuePair> form = new ArrayList<>();
                for (var par : request.getPostParameters().entrySet()) {
                    form.add(new BasicNameValuePair(par.getKey(), par.getValue()));
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);
                ((HttpEntityEnclosingRequestBase) fullRequest).setEntity(entity);
            } else if (request.isMultipart()) {
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                for (MultipartPart part : request.getMultipartData()) {
                    var contentDispositionString = RequestUtils.getFromMap(part.getHeaders(), "Content-Disposition");
                    var contentDisposition = RequestUtils.parseContentDisposition(contentDispositionString);
                    if (MimeChecker.isBinary(contentDisposition.get("type"),null)) {
                        builder.addBinaryBody(
                                contentDisposition.get("name"), Base64.getDecoder().decode(part.getData()), ContentType.create(contentDisposition.get("type")), contentDisposition.get("filename"));
                    } else {
                        builder.addTextBody(
                                contentDisposition.get("name"), part.getData(), ContentType.TEXT_PLAIN);
                    }
                }
                HttpEntity entity = builder.build();
                ((HttpEntityEnclosingRequestBase) fullRequest).setEntity(entity);
            } else if (request.hasBody()) {
                HttpEntity entity;
                if (request.isBinaryRequest()) {
                    entity = new ByteArrayEntity(
                            (byte[]) request.getRequest(),
                            ContentType.create(request.getHeader("content-type")));

                } else {
                    entity = new StringEntity(
                            (String) request.getRequest(),
                            ContentType.create(request.getHeader("content-type")));

                }
                ((HttpEntityEnclosingRequestBase) fullRequest).setEntity(entity);
            }

            try {
                HttpResponse httpResponse = httpClient.execute(fullRequest);
                Response.fromHttpResponse(httpResponse, response);
            } catch (Exception ex) {
                response.setStatusCode(404);
            }
        }finally {
            if(fullRequest!=null){
                fullRequest.releaseConnection();
            }
        }
    }

    private void sendResponse(Response response, HttpExchange httpExchange) throws IOException {
        byte[] data = new byte[0];
        var dataLength = -1;
        if(response.getResponse()!=null) {
            if (response.isBinaryResponse()) {
                data = ((byte[]) response.getResponse());
            } else if(((String) response.getResponse()).length()>0){
                data = (((String) response.getResponse()).getBytes(StandardCharsets.UTF_8));
            }
            if(data.length>0){
                dataLength = data.length;
            }
        }
        /*
        Access-Control-Allow-Origin: https://foo.bar.org
Access-Control-Allow-Methods: POST, GET, OPTIONS, DELETE
Access-Control-Allow-Headers: Content-Type, x-requested-with
Access-Control-Max-Age: 86400
         */
        response.getHeaders().put("Access-Control-Allow-Origin","*");
        response.getHeaders().put("Access-Control-Allow-Methods","*");
        response.getHeaders().put("Access-Control-Allow-Headers","*");
        response.getHeaders().put("Access-Control-Max-Age","86400");
        var duplicate = new HashSet<String>();
        for (var header: response.getHeaders().entrySet()) {
            var keyLow = header.getKey().toLowerCase(Locale.ROOT);
            if(duplicate.contains(keyLow))continue;
            duplicate.add(keyLow);
            httpExchange.getResponseHeaders().add(header.getKey(), header.getValue());
        }
        httpExchange.sendResponseHeaders(response.getStatusCode(), dataLength);

        if(dataLength>0) {
            OutputStream os = httpExchange.getResponseBody();
            os.write(data);
            os.close();
        }
        httpExchange.close();
    }

    private HttpRequestBase createFullRequest(Request request, String fullAddress) throws Exception {
        if(request.getMethod().equalsIgnoreCase("POST")){
            return new HttpPost(fullAddress);
        }else if(request.getMethod().equalsIgnoreCase("PUT")){
            return new HttpPut(fullAddress);
        }else if(request.getMethod().equalsIgnoreCase("PATCH")){
            return new HttpPatch(fullAddress);
        }else if(request.getMethod().equalsIgnoreCase("GET")){
            return new HttpGet(fullAddress);
        }else if(request.getMethod().equalsIgnoreCase("DELETE")){
            return new HttpDelete(fullAddress);
        }else if(request.getMethod().equalsIgnoreCase("HEAD")){
            return new HttpHead(fullAddress);
        }else if(request.getMethod().equalsIgnoreCase("OPTIONS")){
            return new HttpOptions(fullAddress);
        }else if(request.getMethod().equalsIgnoreCase("TRACE")){
            return new HttpTrace(fullAddress);
        }else{
            logger.error("MISSING METHOD "+request.getMethod()+" on "+fullAddress);
            throw new Exception("MISSING METHOD "+request.getMethod()+" on "+fullAddress);
        }
    }

    private String buildFullAddress(Request request) {

        String port = "";
        if(request.getPort()!=-1) {
            if (request.getPort() != 443 && request.getProtocol().equalsIgnoreCase("https")) {
                port = ":" + Integer.toString(request.getPort());
            }

            if (request.getPort() != 80 && request.getProtocol().equalsIgnoreCase("http")) {
                port = ":" + Integer.toString(request.getPort());
            }
        }
        return request.getProtocol()+"://"+request.getHost()+port+request.getPath()+buildFullQuery(request);
    }

    private String buildFullQuery(Request request) {
        if(request.getQuery().size()==0) return "";
        return "?" + request.getQuery().entrySet()
                .stream()
                .map(e -> {
                    try {
                        return e.getKey()+"="+java.net.URLEncoder.encode(e.getValue(), "UTF-8").replace(" ", "%20");
                    } catch (UnsupportedEncodingException unsupportedEncodingException) {
                        return e.getKey()+"="+e.getValue();
                    }
                })
                .collect(joining("&"));

    }

    private HttpEntity handleSoapRequest(Request request, HttpExchange httpExchange) {
        return null;
    }

    public void setHttpsForwardProtocol(String httpsForwardProtocol) {
        this.httpsForwardProtocol = httpsForwardProtocol;
    }

    public void setHttpForwardProtocol(String httpForwardProtocol) {
        this.httpForwardProtocol = httpForwardProtocol;
    }

    public void setHttpsForwardPort(int httpsForwardPort) {
        this.httpsForwardPort = httpsForwardPort;
    }

    public void setHttpForwardPort(int httpForwardPort) {
        this.httpForwardPort = httpForwardPort;
    }

}
