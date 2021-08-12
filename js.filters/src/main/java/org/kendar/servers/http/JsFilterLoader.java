package org.kendar.servers.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kendar.http.CustomFilters;
import org.kendar.http.FilterDescriptor;
import org.kendar.utils.FileResourcesUtils;
import org.kendar.utils.LoggerBuilder;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class JsFilterLoader implements CustomFilters {
    @Value("${jsfilter.path:null}")
    private String jsFilterPath = null;
    private ScriptableObject globalScope;
    private Environment environment;
    private Logger logger;
    private LoggerBuilder loggerBuilder;
    private FileResourcesUtils fileResourcesUtils;

    public JsFilterLoader(Environment environment, LoggerBuilder loggerBuilder, FileResourcesUtils fileResourcesUtils){

        this.environment = environment;
        this.logger = loggerBuilder.build(JsFilterLoader.class);
        this.loggerBuilder = loggerBuilder;
        this.fileResourcesUtils = fileResourcesUtils;
        logger.info("JsFilter LOADED");
    }


    private static ObjectMapper mapper = new ObjectMapper();


    public static class SandboxClassShutter implements ClassShutter {
        public boolean visibleToScripts(String fullClassName) {
            return fullClassName.equals(HashMap.class.getName());
        }
    }

    protected Scriptable getNewScope(Context cx) {
        //global scope lazy initialization
        if (globalScope == null) {
            globalScope = cx.initStandardObjects();
        }

        return cx.newObject(globalScope);
    }

    public List<FilterDescriptor> loadFilters() {
        var result = new ArrayList<FilterDescriptor>();
        String currentPath = "";
        //https://parsiya.net/blog/2019-12-22-using-mozilla-rhino-to-run-javascript-in-java/
        try {
            File f = null;
            var realPath=fileResourcesUtils.buildPath(jsFilterPath);
            f = new File(realPath);
            if(f.exists()) {
                var pathnames = f.list();
                // For each pathname in the pathnames array
                for (String pathname : pathnames) {
                    var fullPath = fileResourcesUtils.buildPath(realPath,pathname);
                    currentPath= fullPath;
                    var newFile = new File(fullPath);
                    if(newFile.isFile()){
                        var data = Files.readString(Path.of(fullPath));
                        var filterDescriptor = mapper.readValue(data, JsFilterDescriptor.class);
                        filterDescriptor.setRoot(realPath);
                        precompileFilter(filterDescriptor);
                        var executor = new JsFilterExecutor(filterDescriptor,this,loggerBuilder,filterDescriptor.getId());
                        var fd = new FilterDescriptor(executor,environment);
                        fd.setEnabled(filterDescriptor.isEnabled());
                        result.add(fd);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error compiling js filter "+currentPath,e);
        }
        return result;
    }

    private static final SandboxClassShutter sandboxClassShutter = new SandboxClassShutter();

    private void precompileFilter(JsFilterDescriptor filterDescriptor) throws Exception {
        String scriptSrc = "var globalFilterResult=runFilter(JSON.parse(REQUESTJSON),JSON.parse(RESPONSEJSON));\n" +
                "globalResult.put('request', JSON.stringify(globalFilterResult.request));\n"+
                "globalResult.put('response', JSON.stringify(globalFilterResult.response));\n"+
                "globalResult.put('continue', globalFilterResult.continue);\n";
        //Load all scripts

        for (var file :filterDescriptor.getRequires()) {
            scriptSrc +="\r\n"+ Files.readString(Path.of(filterDescriptor.getRoot()+File.separator+file));
        }

        Context cx = Context.enter();
        cx.setOptimizationLevel(9);
        cx.setLanguageVersion(Context.VERSION_1_8);
        cx.setClassShutter(sandboxClassShutter);
        try {
            Scriptable currentScope = getNewScope(cx);
            filterDescriptor.setSource(scriptSrc);
            filterDescriptor.setScript(cx.compileString(scriptSrc, "my_script_id", 1, null));
        }catch (Exception ex){
            logger.error("Error compiling script");
            logger.error(scriptSrc);
            throw new Exception(ex);
        } finally {
            Context.exit();
        }
    }
}
