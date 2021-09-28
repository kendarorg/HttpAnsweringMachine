package org.kendar.test;

import org.junit.Test;
import org.mozilla.javascript.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsTest {
    private static final String contextFoo ="// Define the Foo module\n" +
            "var Foo = (function() {\n" +
            "   function fib(n) {\n" +
            "       if(n <= 1) return n;\n" +
            "       return fib(n-1) + fib(n-2);\n" +
            "   }\n" +
            "   // Export public functions of the module\n" +
            "   return {\n" +
            "       fib: fib\n" +
            "   };\n" +
            "})();";
/*
    @Test
    public void complexExecute() throws IOException {
        JsTest main = new JsTest();
        Script result = main.compileNoResult(contextFoo);
        main.execComplex(result);
    }

    public void execComplex(Script script){
        Context cx = Context.enter();
        cx.setOptimizationLevel(9);
        cx.setLanguageVersion(Context.VERSION_1_8);
        cx.setClassShutter(sandboxClassShutter);
        try {
            Scriptable currentScope = getNewScope(cx);
            currentScope.put("result", currentScope, result);
            script.exec(cx, currentScope);
            System.out.println("Result: " + result.get("result"));
        } finally {
            Context.exit();
        }
    }*/

    private static final String scriptSource="result.put('result', fib(NUM_CONST).toString());\n" +
            "function fib(n) {\n" +
            " if(n <= 1) return n;\n" +
            "return fib(n-1) + fib(n-2);\n" +
            "}";

    @Test
    public void simplExecute() throws IOException {
        JsTest main = new JsTest();
        main.execute(scriptSource);
    }

    @Test
    public void compileAndRun() throws IOException {
        JsTest main = new JsTest();
        Script result = main.compileNoResult(scriptSource);
        main.execWithResult(result);
    }

    private static final SandboxClassShutter sandboxClassShutter = new SandboxClassShutter();

    private static ScriptableObject globalScope;

    public void execWithResult(Script script){
        Context cx = Context.enter();
        cx.setOptimizationLevel(9);
        cx.setLanguageVersion(Context.VERSION_1_8);
        cx.setClassShutter(sandboxClassShutter);
        try {
            Map result = new HashMap<>();
            Scriptable currentScope = getNewScope(cx);
            currentScope.put("NUM_CONST", globalScope, NUM_CONST);
            currentScope.put("result", currentScope, result);
            script.exec(cx, currentScope);
            System.out.println("Result: " + result.get("result"));
        } finally {
            Context.exit();
        }
    }

    public Script compileNoResult(String src) {
        Context cx = Context.enter();
        cx.setOptimizationLevel(9);
        cx.setLanguageVersion(Context.VERSION_1_8);
        cx.setClassShutter(sandboxClassShutter);
        try {
            Map result = new HashMap<>();
            Scriptable currentScope = getNewScope(cx);
            currentScope.put("NUM_CONST", globalScope, NUM_CONST);
            Script script = cx.compileString(
                    src,
                    "my_script_id", 1, null);
            return script;
        } finally {
            Context.exit();
        }
    }


    public void execute(String src) {
        Context cx = Context.enter();
        cx.setOptimizationLevel(9);
        cx.setLanguageVersion(Context.VERSION_1_8);
        cx.setClassShutter(sandboxClassShutter);
        try {
            Map result = new HashMap<>();
            Scriptable currentScope = getNewScope(cx);

            currentScope.put("NUM_CONST", globalScope, NUM_CONST);
            currentScope.put("result", currentScope, result);
            Script script = cx.compileString(
                    src,
                    "my_script_id", 1, null);

            script.exec(cx, currentScope);
            System.out.println("Result: " + result.get("result"));
        } finally {
            Context.exit();
        }
    }

    private static final int NUM_CONST = 20;
    private Scriptable getNewScope(Context cx) {
        //global scope lazy initialization
        if (globalScope == null) {
            globalScope = cx.initStandardObjects();
        }

        return cx.newObject(globalScope);
    }

    public static class SandboxClassShutter implements ClassShutter {
        public boolean visibleToScripts(String fullClassName) {
            return fullClassName.equals(HashMap.class.getName());
        }
    }
}
