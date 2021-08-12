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
    private JsFilterDescriptor filterDescriptor;
    private JsFilterLoader jsFilterLoader;

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
        cx.setClassShutter(sandboxClassShutter);
        try {
            Map result = new HashMap<>();
            Scriptable currentScope = jsFilterLoader.getNewScope(cx);
            currentScope.put("REQUESTJSON", currentScope,
                    mapper.writeValueAsString(Request.toSerializable(request)));
            currentScope.put("RESPONSEJSON", currentScope,
                    mapper.writeValueAsString(Response.toSerializable(response)));
            currentScope.put("globalResult", currentScope, result);
            filterDescriptor.getScript().exec(cx, currentScope);
            fromJsonRequest(request,(String)result.get("request"));
            fromJsonResponse(response,(String)result.get("response"));

            var isBlocking =!(boolean)result.get("continue");
            if(response.getStatusCode()==500){
                logger.error(response.getResponse().toString());
            }
            return isBlocking;
        }catch (Exception ex){
            response.setStatusCode(500);
            response.setResponse(ex.getMessage());
            logger.error(ex.getMessage(),ex);
            return false;
        } finally {
            Context.exit();
        }
    }

    private void fromJsonResponse(Response response, String response1) throws Exception {
        try {
            var serResponse = mapper.readValue(response1,SerializableResponse.class);
            Response.fromSerializable(response,serResponse);
        } catch (JsonProcessingException e) {
            throw new Exception("Unable to deserialize response");
        }
    }

    private void fromJsonRequest(Request request, String request1) throws Exception {
        try {
            var serRequest = mapper.readValue(request1,SerializableRequest.class);
            Request.fromSerializable(request,serRequest);
        } catch (JsonProcessingException e) {
            throw new Exception("Unable to deserialize response");
        }
    }

    private static ObjectMapper mapper = new ObjectMapper();


}
