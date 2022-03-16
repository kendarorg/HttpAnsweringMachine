package org.kendar.replayer.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class JsReplayerExecutor {
    private static final Pattern LINE_SEP_PATTERN = Pattern.compile("\\R");
    private ScriptableObject globalScope;
    private ObjectMapper mapper = new ObjectMapper();

    protected Scriptable getNewScope(Context cx) {
        // global scope lazy initialization
        if (globalScope == null) {
            globalScope = cx.initStandardObjects();
        }

        return cx.newObject(globalScope);
    }

    public Script prepare(String data){
        if(data==null||data.isEmpty()) return null;
        StringBuilder scriptSrc =
                new StringBuilder(
                        "var globalFilterResult = JSON.stringify(runFilter(RUNID,JSON.parse(REQUESTJSON),JSON.parse(RESPONSEJSON),JSON.parse(EXPECTEDRESPONSEJSON)));\n"
                                + "if(globalFilterResult!=null)globalResult.put('request', JSON.stringify(globalFilterResult.request));\n"
                                + "if(globalFilterResult!=null)globalResult.put('response', JSON.stringify(globalFilterResult.response));\n");
        scriptSrc.append("\r\nfunction runFilter(runid,request,response,expectedresponse){");
        String[] lines = LINE_SEP_PATTERN.split(data);
        for (var sourceLine :lines) {
            scriptSrc
                    .append("\r\n")
                    .append(sourceLine);
        }
        scriptSrc.append("\r\n}");
        try {
            Context cx = Context.enter();
            cx.setOptimizationLevel(9);
            cx.setLanguageVersion(Context.VERSION_1_8);
            Scriptable currentScope = getNewScope(cx);
            return cx.compileString(scriptSrc.toString(), "my_script_id", 1, null);
        }catch (Exception ex){
            return null;
        }finally {
            Context.exit();
        }
    }

    public void run(String id,Request request, Response response,Response expectedResponse,Script script) throws Exception{
        Context cx = Context.enter();
        cx.setOptimizationLevel(9);
        cx.setLanguageVersion(Context.VERSION_1_8);
        cx.initStandardObjects();

        try {
            if(expectedResponse==null){
                expectedResponse = new Response();
            }
            Map<Object, Object> result = new HashMap<>();
            Scriptable currentScope = getNewScope(cx);
            currentScope.put("RUNID", currentScope,
                    id);
            currentScope.put("REQUESTJSON", currentScope,
                    mapper.writeValueAsString(request));
            currentScope.put("RESPONSEJSON", currentScope,
                    mapper.writeValueAsString(response));
            currentScope.put("globalResult", currentScope, result);
            currentScope.put("EXPECTEDRESPONSEJSON", currentScope,
                    mapper.writeValueAsString(expectedResponse));
            //
            //cx.setClassShutter(sandboxClassShutter);
            script.exec(cx, currentScope);
            if(result.containsKey("response")) {
                fromJsonResponse(response, (String) result.get("response"));
            }
            if(result.containsKey("request")) {
                fromJsonRequest(request, (String) result.get("request"));
            }
        }finally {
            Context.exit();
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
}
