package org.kendar.servers.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.servers.http.matchers.FilterMatcher;
import org.kendar.http.GenericFilterExecutor;
import org.kendar.http.HttpFilterType;
import org.kendar.servers.http.types.http.JsHttpFilterDescriptor;
import org.kendar.utils.LoggerBuilder;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class JsFilterExecutor extends GenericFilterExecutor {
    private final Logger logger;
    private final JsHttpFilterDescriptor filterDescriptor;
    private final JsFilterLoader jsFilterLoader;

    public JsFilterExecutor(JsHttpFilterDescriptor filterDescriptor, JsFilterLoader jsFilterLoader, LoggerBuilder loggerBuilder,
                            String id,FilterMatcher ... matchers) {
        super(filterDescriptor.getPriority(),
                filterDescriptor.isBlocking(),filterDescriptor.isBlocking(),

                HttpFilterType.valueOf(filterDescriptor.getPhase()),null,null,matchers);
        setId(id);
        this.logger = loggerBuilder.build(JsFilterExecutor.class);
        this.filterDescriptor = filterDescriptor;
        this.jsFilterLoader = jsFilterLoader;
    }

    private static final JsFilterLoader.SandboxClassShutter sandboxClassShutter = new JsFilterLoader.SandboxClassShutter();

    @Override
    public boolean run(Request request, Response response) {
        if(filterDescriptor.getAction().getType().equalsIgnoreCase("body")){
            response.setResponseText(filterDescriptor.getAction().getSource());
            return true;
        }
        Context cx = Context.enter();
        cx.setOptimizationLevel(9);
        cx.setLanguageVersion(Context.VERSION_1_8);
        cx.initStandardObjects();

        try {
            Map<Object, Object> result = new HashMap<>();
            Scriptable currentScope = jsFilterLoader.getNewScope(cx);
            currentScope.put("REQUESTJSON", currentScope,request);
            currentScope.put("RESPONSEJSON", currentScope,response);
            currentScope.put("globalResult", currentScope, result);
            currentScope.put("utils",currentScope,
                    Context.toObject(filterDescriptor.retrieveQueue(),currentScope));
            //
            //cx.setClassShutter(sandboxClassShutter);
            filterDescriptor.getScript().exec(cx, currentScope);

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

    private static final ObjectMapper mapper = new ObjectMapper();


}
