package org.kendar.servers.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.GenericFilterExecutor;
import org.kendar.http.HttpFilterType;
import org.kendar.utils.LoggerBuilder;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class JsFilterExecutor extends GenericFilterExecutor {
    private final Logger logger;
    private final JsFilterDescriptor filterDescriptor;
    private final JsFilterLoader jsFilterLoader;

    public JsFilterExecutor(JsFilterDescriptor filterDescriptor, JsFilterLoader jsFilterLoader, LoggerBuilder loggerBuilder,String id) {
        super(filterDescriptor.getPriority(),
                filterDescriptor.getMethod(),
                filterDescriptor.isBlocking(),filterDescriptor.isBlocking(),
                filterDescriptor.getHostAddress(),filterDescriptor.getPathAddress(),
                filterDescriptor.getHostRegexp(),filterDescriptor.getPathRegexp(),

                HttpFilterType.valueOf(filterDescriptor.getPhase()),null,null);
        setId(id);
        this.logger = loggerBuilder.build(JsFilterExecutor.class);
        this.filterDescriptor = filterDescriptor;
        this.jsFilterLoader = jsFilterLoader;
    }

    private static final JsFilterLoader.SandboxClassShutter sandboxClassShutter = new JsFilterLoader.SandboxClassShutter();

    @Override
    public boolean run(Request request, Response response) {
        Context cx = Context.enter();
        cx.setOptimizationLevel(9);
        cx.setLanguageVersion(Context.VERSION_1_8);
        cx.initStandardObjects();

        try {
            Map<Object, Object> result = new HashMap<>();
            Scriptable currentScope = jsFilterLoader.getNewScope(cx);
            currentScope.put("REQUESTJSON", currentScope,
                    mapper.writeValueAsString(request));
            currentScope.put("RESPONSEJSON", currentScope,
                    mapper.writeValueAsString(response));
            currentScope.put("globalResult", currentScope, result);
            currentScope.put("utils",currentScope,
                    Context.toObject(filterDescriptor.retrieveQueue(),currentScope));
            //
            //cx.setClassShutter(sandboxClassShutter);
            filterDescriptor.getScript().exec(cx, currentScope);
            fromJsonRequest(request,(String)result.get("request"));
            fromJsonResponse(response,(String)result.get("response"));

            var isBlocking =!(boolean)result.get("continue");
            if(response.getStatusCode()==500){
                logger.error(response.getResponseText());
            }
            return isBlocking;
        }catch (Exception ex){
            response.setStatusCode(500);
            response.setResponseText(ex.getMessage());
            logger.error(ex.getMessage(),ex);
            return false;
        } finally {
            Context.exit();
        }
    }

    private void fromJsonResponse(Response response, String response1) throws Exception {
        try {
            var serResponse = mapper.readValue(response1,Response.class);
            response.setBinaryResponse(serResponse.isBinaryResponse());
            if(serResponse.isBinaryResponse()){
                response.setResponseBytes(serResponse.getResponseBytes());
            }else{
                response.setResponseText(serResponse.getResponseText());
            }
            response.setHeaders(serResponse.getHeaders());
            response.setStatusCode(serResponse.getStatusCode());
        } catch (JsonProcessingException e) {
            throw new Exception("Unable to deserialize response");
        }
    }

    private void fromJsonRequest(Request result, String request1) throws Exception {
        try {
            var request = mapper.readValue(request1,Request.class);
            result.setMethod(request.getMethod());
            result.setBinaryRequest(request.isBinaryRequest());
            result.setRequestText(request.getRequestText());
            result.setRequestBytes(request.getRequestBytes());
            result.setHeaders(request.getHeaders());
            result.setProtocol(request.getProtocol());
            result.setSoapRequest(request.isSoapRequest());
            result.setBasicPassword(request.getBasicPassword());
            result.setBasicUsername(request.getBasicUsername());
            result.setMultipartData(request.getMultipartData());
            result.setStaticRequest(request.isStaticRequest());
            result.setHost ( request.getHost());
            result.setPath ( request.getPath());
            result.setPostParameters ( request.getPostParameters());
            result.setPort ( request.getPort());
            result.setQuery (request.getQuery());
        } catch (JsonProcessingException e) {
            throw new Exception("Unable to deserialize response");
        }
    }

    private static final ObjectMapper mapper = new ObjectMapper();


}
