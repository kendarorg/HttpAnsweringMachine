package org.kendar.servers.http.types.http;

import org.kendar.servers.http.JsUtils;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.matchers.FilterMatcher;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class ScriptMatcher implements FilterMatcher {

    private String script;
    private JsUtils jsUtils;
    private Script js;
    private static ScriptableObject globalScope;

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    @Override
    public boolean matches(Request request) {
        Context cx = Context.enter();
        cx.setOptimizationLevel(9);
        cx.setLanguageVersion(Context.VERSION_1_8);
        cx.initStandardObjects();
        Map<Object, Object> result = new HashMap<>();
        Scriptable currentScope = getNewScope(cx);
        currentScope.put("REQUESTJSON", currentScope,request);
        currentScope.put("globalResult", currentScope, result);
        currentScope.put("utils",currentScope,
                Context.toObject(jsUtils,currentScope));

        js.exec(cx, currentScope);

        return (boolean)result.get("continue");
    }

    private Scriptable getNewScope(Context cx) {
        if (globalScope == null) {
            globalScope = cx.initStandardObjects();
        }

        return cx.newObject(globalScope);
    }

    @Override
    public void initialize(Function<String, String> apply) {

    }

    public void initializeUtils(JsUtils jsUtils) {

        this.jsUtils = jsUtils;
    }

    public void intializeScript(Script js) {
        this.js = js;
    }
}
