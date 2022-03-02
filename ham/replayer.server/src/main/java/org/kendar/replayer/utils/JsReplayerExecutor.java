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
                        "var globalFilterResult=runFilter(JSON.parse(REQUESTJSON),JSON.parse(RESPONSEJSON),JSON.parse(EXPECTEDRESPONSEJSON));\n"
                                + "globalResult.put('request', JSON.stringify(globalFilterResult.request));\n"
                                + "globalResult.put('response', JSON.stringify(globalFilterResult.response));\n");
        scriptSrc.append("\r\nfunction runFilter(request,response){");
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

    public void run(Request request, Response response,Response expectedResponse,Script script) throws Exception{
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
            currentScope.put("REQUESTJSON", currentScope,
                    mapper.writeValueAsString(request));
            currentScope.put("RESPONSEJSON", currentScope,
                    mapper.writeValueAsString(response));
            currentScope.put("EXPECTEDRESPONSEJSON", currentScope,
                    mapper.writeValueAsString(expectedResponse));
            //
            //cx.setClassShutter(sandboxClassShutter);
            script.exec(cx, currentScope);
        }finally {
            Context.exit();
        }
    }
}
