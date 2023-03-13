package org.kendar.replayer.utils;

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
    private final ObjectMapper mapper = new ObjectMapper();

    protected Scriptable getNewScope(Context cx) {
        // global scope lazy initialization
        if (globalScope == null) {
            globalScope = cx.initStandardObjects();
        }

        return cx.newObject(globalScope);
    }

    public Script prepare(String data) {
        if (data == null || data.isEmpty()) return null;
        StringBuilder scriptSrc =
                new StringBuilder(
                        "runFilter(RUNID,REQUESTJSON,RESPONSEJSON,EXPECTEDRESPONSEJSON);\n");
        scriptSrc.append("\r\nfunction runFilter(runid,request,response,expectedresponse){");
        String[] lines = LINE_SEP_PATTERN.split(data);
        for (var sourceLine : lines) {
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
        } catch (Exception ex) {
            return null;
        } finally {
            Context.exit();
        }
    }

    public void run(Long id, Request request, Response response, Response expectedResponse, Script script) throws Exception {
        Context cx = Context.enter();
        cx.setOptimizationLevel(9);
        cx.setLanguageVersion(Context.VERSION_1_8);
        cx.initStandardObjects();

        try {
            if (expectedResponse == null) {
                expectedResponse = new Response();
            }
            Map<Object, Object> result = new HashMap<>();
            Scriptable currentScope = getNewScope(cx);
            currentScope.put("RUNID", currentScope,
                    id);
            currentScope.put("REQUESTJSON", currentScope, request);
            currentScope.put("RESPONSEJSON", currentScope, response);
            currentScope.put("EXPECTEDRESPONSEJSON", currentScope, expectedResponse);
            script.exec(cx, currentScope);
        } catch (Exception ex) {
            throw new Exception(ex);
        } finally {
            Context.exit();
        }
    }

}
