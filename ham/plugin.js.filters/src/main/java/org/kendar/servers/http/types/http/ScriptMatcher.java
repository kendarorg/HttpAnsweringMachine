package org.kendar.servers.http.types.http;

import org.kendar.servers.http.JsUtils;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.matchers.FilterMatcher;
import org.kendar.servers.http.matchers.HostMatcher;
import org.kendar.servers.http.matchers.PathMatcher;
import org.kendar.servers.http.matchers.PathSimpleMatcher;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class ScriptMatcher implements FilterMatcher, PathMatcher, HostMatcher {

    private PathSimpleMatcher pathSimpleMatchers = new PathSimpleMatcher();

    private String pathAddress;
    private String hostAddress;

    @Override
    public String getPathAddress() {
        return pathAddress;
    }

    public void setPathAddress(String pathAddress) {
        this.pathAddress = pathAddress;
    }

    @Override
    public String getHostAddress() {
        return hostAddress;
    }

    @Override
    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

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
        if(hostAddress==null) return false;
        if(pathSimpleMatchers.notMatch(request.getHost(),this.hostAddress)){
            return false;
        }
        if(pathSimpleMatchers.matches(request)){
            return true;
        }
        if(pathSimpleMatchers.notMatch(request.getPath(),this.pathAddress)){
            return false;
        }
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
        if(hostAddress!=null)hostAddress = apply.apply(hostAddress);
        if(pathAddress!=null){
            pathAddress = apply.apply(pathAddress);
            pathSimpleMatchers.setupPathSimpleMatchers(pathAddress);
        }
    }

    public void initializeUtils(JsUtils jsUtils) {

        this.jsUtils = jsUtils;
    }

    public void intializeScript(Script js) {
        this.js = js;
    }

    @Override
    public boolean validate() {
        return (isValid(pathAddress)||isValid(hostAddress))&& isValid(script);
    }

    private boolean isValid(String val) {
        return val!=null&&val.length()>0;
    }
}
