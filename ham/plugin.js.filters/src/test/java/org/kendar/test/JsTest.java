package org.kendar.test;

import org.junit.jupiter.api.Test;
import org.mozilla.javascript.*;

import java.util.HashMap;
import java.util.Map;

public class JsTest {
    private static final String contextFoo =
            "// Define the Foo module\n"
                    + "var Foo = (function() {\n"
                    + "   function fib(n) {\n"
                    + "       if(n <= 1) return n;\n"
                    + "       return fib(n-1) + fib(n-2);\n"
                    + "   }\n"
                    + "   // Export public functions of the module\n"
                    + "   return {\n"
                    + "       fib: fib\n"
                    + "   };\n"
                    + "})();";

    private static final String scriptSource =
            "result.put('result', fib(NUM_CONST).toString());\n"
                    + "function fib(n) {\n"
                    + " if(n <= 1) return n;\n"
                    + "return fib(n-1) + fib(n-2);\n"
                    + "}";
    private static final SandboxClassShutter sandboxClassShutter = new SandboxClassShutter();
    private static final int NUM_CONST = 20;
    private static ScriptableObject globalScope;

    @Test
    public void simplExecute() {
        JsTest main = new JsTest();
        main.execute(scriptSource);
    }

    @Test
    public void compileAndRun() {
        JsTest main = new JsTest();
        Script result = main.compileNoResult(scriptSource);
        main.execWithResult(result);
    }

    public void execWithResult(Script script) {
        Context cx = Context.enter();
        cx.setOptimizationLevel(9);
        cx.setLanguageVersion(Context.VERSION_1_8);
        cx.setClassShutter(sandboxClassShutter);
        try {
            Map<Object, Object> result = new HashMap<>();
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
            Map<Object, Object> result = new HashMap<>();
            Scriptable currentScope = getNewScope(cx);
            currentScope.put("NUM_CONST", globalScope, NUM_CONST);
            return cx.compileString(src, "my_script_id", 1, null);
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
            Map<Object, Object> result = new HashMap<>();
            Scriptable currentScope = getNewScope(cx);

            currentScope.put("NUM_CONST", globalScope, NUM_CONST);
            currentScope.put("result", currentScope, result);
            Script script = cx.compileString(src, "my_script_id", 1, null);

            script.exec(cx, currentScope);
            System.out.println("Result: " + result.get("result"));
        } finally {
            Context.exit();
        }
    }

    private Scriptable getNewScope(Context cx) {
        // global scope lazy initialization
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
