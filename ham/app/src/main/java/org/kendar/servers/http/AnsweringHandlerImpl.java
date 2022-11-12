package org.kendar.servers.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsExchange;
import org.apache.http.conn.HttpClientConnectionManager;
import org.kendar.events.EventQueue;
import org.kendar.http.FilteringClassesHandler;
import org.kendar.http.HttpFilterType;
import org.kendar.remote.ExecuteLocalRequest;
import org.kendar.remote.ExecuteRemoteRequest;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.config.GlobalConfig;
import org.kendar.servers.proxy.SimpleProxyHandler;
import org.kendar.utils.*;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;

@Component
public class AnsweringHandlerImpl implements AnsweringHandler {
  public static final String MIRROR_REQUEST_HEADER = "X-MIRROR-REQUEST";
  public static final String TEST_EXPECT_100 = "X-TEST-EXPECT-100";
  public static final String TEST_OVERWRITE_HOST = "X-TEST-OVERWRITE-HOST";
  private final Logger logger;
  private final FilteringClassesHandler filteringClassesHandler;
  private final SimpleProxyHandler simpleProxyHandler;
  private final RequestResponseBuilder requestResponseBuilder;
  private final ObjectMapper mapper = new ObjectMapper();
  private final Logger requestLogger;
  private final JsonConfiguration configuration;
  private final ExternalRequester externalRequester;
  private final ConnectionBuilder connectionBuilder;

  public AnsweringHandlerImpl(
          LoggerBuilder loggerBuilder,
          FilteringClassesHandler filteringClassesHandler,
          SimpleProxyHandler simpleProxyHandler,
          RequestResponseBuilder requestResponseBuilder,
          PluginsInitializer pluginsInitializer,
          JsonConfiguration configuration,
          ExternalRequester externalRequester,
          ConnectionBuilder connectionBuilder,
          EventQueue eventQueue) {
    this.logger = loggerBuilder.build(AnsweringHandlerImpl.class);

    this.requestLogger = loggerBuilder.build(Request.class);
    this.filteringClassesHandler = filteringClassesHandler;
    this.simpleProxyHandler = simpleProxyHandler;

    this.requestResponseBuilder = requestResponseBuilder;
    this.configuration = configuration;
    this.externalRequester = externalRequester;
    this.connectionBuilder = connectionBuilder;
    pluginsInitializer.addSpecialLogger(
        Request.class.getName(), "Requests Logging (INFO,DEBUG,TRACE)");
    pluginsInitializer.addSpecialLogger(
        Response.class.getName(), "Responses Logging (DEBUG,TRACE)");
    pluginsInitializer.addSpecialLogger(
        StaticRequest.class.getName(), "Log static requests as file (DEBUG), precedence over Req/Res");
    pluginsInitializer.addSpecialLogger(
            DynamicReqest.class.getName(), "Log dynamic requests as file (DEBUG), precedence over Req/Res");
    pluginsInitializer.addSpecialLogger(
            InternalRequest.class.getName(), "Log internal requests (DEBUG)");



    eventQueue.registerCommand(e->{return remoteRequest(e);}, ExecuteRemoteRequest.class);
    eventQueue.registerCommand(e->{return localRequest(e);}, ExecuteLocalRequest.class);
  }


  private void mirrorRequest(Request request, HttpExchange httpExchange) {
    try {
      String myObjectInJson = mapper.writeValueAsString(request);
      httpExchange
          .getResponseHeaders()
          .put("Access-Control-Allow-Origin", Collections.singletonList("*"));
      httpExchange.sendResponseHeaders(200, myObjectInJson.getBytes().length);

      OutputStream os = httpExchange.getResponseBody();
      os.write(myObjectInJson.getBytes());
      os.close();
      httpExchange.close();
    } catch (Exception ex) {
      logger.error("Error mirroring request ", ex);
    }
  }

  private boolean testExpect100(Request request, HttpExchange httpExchange) {
    String expect100 = request.getHeader(TEST_EXPECT_100);
    if (expect100 != null) {
      mirrorRequest(request, httpExchange);
      return true;
    }
    return false;
  }

  private void handleOverwriteHost(Request request) {
    String overwriteHost = request.getHeader(TEST_OVERWRITE_HOST);
    if (overwriteHost != null) {
      try {
        URL url = new URL(overwriteHost);
        request.setProtocol(url.getProtocol().toLowerCase(Locale.ROOT));
        request.setHost(url.getHost());
        if (url.getPort() == -1 && request.getProtocol().equalsIgnoreCase("https")) {
          request.setPort(443);
        } else if (url.getPort() == -1 && request.getProtocol().equalsIgnoreCase("http")) {
          request.setPort(80);
        } else if (url.getPort() > 0) {
          request.setPort(url.getPort());
        }
      } catch (MalformedURLException e) {
        // e.printStackTrace();
      }
    }
  }



  public boolean mirrorData(Request request, HttpExchange httpExchange) {
    String isMirror = request.getHeader(MIRROR_REQUEST_HEADER);
    if (isMirror != null) {
      mirrorRequest(request, httpExchange);
      return true;
    }
    return false;
  }

  private Response remoteRequest(ExecuteRemoteRequest e) {
    try {
      var connManager = connectionBuilder.getConnectionManger(true, true);
      var config = configuration.getConfiguration(GlobalConfig.class);
      var response = new Response();
      var request = e.getRequest();
      try {
        handleInternal(request, response, config, connManager);

      } catch (Exception ex) {
        throw new RuntimeException(ex);
      } finally {
        filteringClassesHandler.handle(
                config, HttpFilterType.POST_RENDER, request, response, connManager);
      }
      return response;
    }catch (InvocationTargetException| IllegalAccessException ex){
      return null;
    }
  }

  private Response localRequest(ExecuteLocalRequest e) {
    try {
      var connManager = connectionBuilder.getConnectionManger(false, true);
      var config = configuration.getConfiguration(GlobalConfig.class);
      var response = new Response();
      var request = e.getRequest();
      try {
        handleInternal(request, response, config, connManager);

      } catch (Exception ex) {
        throw new RuntimeException(ex);
      } finally {
        filteringClassesHandler.handle(
                config, HttpFilterType.POST_RENDER, request, response, connManager);
      }
      return response;
    }catch (InvocationTargetException| IllegalAccessException ex){
      return null;
    }
  }

  private void handleInternal(Request request, Response response, GlobalConfig config, HttpClientConnectionManager connManager) throws Exception {
    if (filteringClassesHandler.handle(
            config, HttpFilterType.PRE_RENDER, request, response, connManager)) {

      return;
    }

    if (filteringClassesHandler.handle(
            config, HttpFilterType.API, request, response, connManager)) {
      // ALWAYS WHEN CALLED
      return;
    }

    if (filteringClassesHandler.handle(
            config, HttpFilterType.STATIC, request, response, connManager)) {
      // ALWAYS WHEN CALLED
      return;
    }

    request = simpleProxyHandler.translate(request);

    if (filteringClassesHandler.handle(
            config, HttpFilterType.PRE_CALL, request, response, connManager)) {
      return;
    }

    externalRequester.callSite(request, response);

    if (filteringClassesHandler.handle(
            config, HttpFilterType.POST_CALL, request, response, connManager)) {
      return;
    }
  }

  @Override
  public void handle(HttpExchange httpExchange) {

    var requestUri = httpExchange.getRequestURI();
    var host = httpExchange.getRequestHeaders().getFirst("Host");
    if (requestLogger.isInfoEnabled()
        || requestLogger.isDebugEnabled()
        || requestLogger.isTraceEnabled()) {
      logger.info(host + requestUri.toString());
    }
    var connManager = connectionBuilder.getConnectionManger(true,true);

    Request request = null;
    Response response = new Response();
    var config = configuration.getConfiguration(GlobalConfig.class);
    try {
      if (httpExchange instanceof HttpsExchange) {
        request = requestResponseBuilder.fromExchange(httpExchange, "https");
      } else {
        request = requestResponseBuilder.fromExchange(httpExchange, "http");
      }

      //START REQUEST
      if(request.getHeader("X-BLOCK-RECURSIVE")!=null){
        var uri = new URI(request.getHeader("X-BLOCK-RECURSIVE"));
        if(uri.getHost().equalsIgnoreCase(request.getHost()) &&
                uri.getPath().equalsIgnoreCase(request.getPath())) {
          response.addHeader("ERROR","Recursive call on "+request.getHeader("X-BLOCK-RECURSIVE"));
          response.setStatusCode(404);
          sendResponse(response,httpExchange);
          return;
        }

      }

      handleOverwriteHost(request);

      if (handleSpecialRequests(httpExchange, request)) {
        return;
      }

      handleInternal(request,response,config,connManager);
      sendResponse(response, httpExchange);

    } catch (Exception rex) {
      handleException(httpExchange, response, rex);
      try {
        httpExchange.close();
      }catch(Exception exx){}
    } finally {
      try {
        filteringClassesHandler.handle(
            config, HttpFilterType.POST_RENDER, request, response, connManager);

      } catch (Exception e) {
        logger.error("ERROR CALLING POST RENDER ", e);
      }
    }
  }

  private void handleException(HttpExchange httpExchange, Response response, Exception ex) {
    try {
      logger.error("ERROR HANDLING HTTP REQUEST ", ex);
      if (response.getHeader(ConstantsHeader.CONTENT_TYPE) == null) {
        response.addHeader(ConstantsHeader.CONTENT_TYPE, ConstantsMime.HTML);
      }
      response.addHeader("X-Exception-Type", ex.getClass().getName());
      response.addHeader("X-Exception-Message", ex.getMessage());
      response.addHeader("X-Exception-PrevStatusCode", Integer.toString(response.getStatusCode()));
      response.setStatusCode(500);
      if (!requestResponseBuilder.hasBody(response)) {
        response.setResponseText(ex.getMessage());
        response.setBinaryResponse(false);
      }
      sendResponse(response, httpExchange);
    } catch (Exception xx) {
      logger.trace(xx.getMessage());
    }
  }

  private boolean handleSpecialRequests(HttpExchange httpExchange, Request request) {
    return mirrorData(request, httpExchange)
        || testExpect100(request, httpExchange)
        || testExpect100(request, httpExchange);
  }



  private void sendResponse(Response response, HttpExchange httpExchange) throws IOException {
    byte[] data = new byte[0];
    var dataLength = 0;
    if (requestResponseBuilder.hasBody(response)) {
      if (response.isBinaryResponse()) {
        data = response.getResponseBytes();
      } else if (response.getResponseText().length() > 0) {

        if(ConstantsMime.JSON_SMILE.equalsIgnoreCase(response.getHeader(ConstantsHeader.CONTENT_TYPE))){
          data = JsonSmile.jsonToSmile(response.getResponseText());
        }else {
          data = (response.getResponseText().getBytes(StandardCharsets.UTF_8));
        }
      }
      if (data.length > 0) {
        dataLength = data.length;
      }
    }
    /*
            Access-Control-Allow-Origin: https://foo.bar.org
    Access-Control-Allow-Methods: POST, GET, OPTIONS, DELETE
    Access-Control-Allow-Headers: Content-Type, x-requested-with
    Access-Control-Max-Age: 86400
             */
    response.addHeader("access-control-allow-credentials","false");
    response.addHeader("Access-Control-Allow-Origin", "*");
    response.addHeader("Access-Control-Allow-Methods", "*");
    response.addHeader("Access-Control-Allow-Headers", "*");
    response.addHeader("Access-Control-Max-Age", "86400");
    response.addHeader("Access-Control-Expose-Headers", "*");
    for (var header : response.getHeaders().entrySet()) {
      httpExchange.getResponseHeaders().add(header.getKey(), header.getValue());
    }
    try {
      httpExchange.sendResponseHeaders(response.getStatusCode(), dataLength);
    }catch (IOException ex){
      if(!ex.getMessage().equalsIgnoreCase("output stream is closed")){
        throw new IOException(ex);
      }
    }

    try {
      if (dataLength > 0) {
        OutputStream os = httpExchange.getResponseBody();
        os.write(data);
        os.flush();
        os.close();
      } else {
        try {
            OutputStream os = httpExchange.getResponseBody();

            os.write(new byte[0]);
            os.flush();
            os.close();
        } catch (Exception ex) {
          //logger.trace(ex.getMessage());
        }
      }
    } catch (Exception ex) {
      //logger.error(ex.getMessage(), ex);
    }
  }
}
