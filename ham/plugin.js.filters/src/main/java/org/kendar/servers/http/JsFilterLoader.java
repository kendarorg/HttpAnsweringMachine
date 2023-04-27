package org.kendar.servers.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.kendar.events.EventQueue;
import org.kendar.http.CustomFiltersLoader;
import org.kendar.http.FilterDescriptor;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.db.HibernateSessionFactory;
import org.kendar.servers.http.matchers.FilterMatcher;
import org.kendar.servers.http.matchers.MatchersRegistry;
import org.kendar.servers.http.storage.DbFilter;
import org.kendar.servers.http.storage.DbFilterRequire;
import org.kendar.servers.http.types.http.JsHttpAction;
import org.kendar.servers.http.types.http.JsHttpFilterDescriptor;
import org.kendar.servers.http.types.http.ScriptMatcher;
import org.kendar.utils.LoggerBuilder;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class JsFilterLoader implements CustomFiltersLoader {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final SandboxClassShutter sandboxClassShutter = new SandboxClassShutter();
    private static final TypeReference<HashMap<String, String>> typeRef
            = new TypeReference<>() {
    };
    private final JsonConfiguration configuration;
    private final EventQueue eventQueue;
    private final ExternalRequester externalRequester;
    private final HibernateSessionFactory sessionFactory;
    private final MatchersRegistry matchersRegistry;
    private final Environment environment;
    private final Logger logger;
    private final LoggerBuilder loggerBuilder;
    private ScriptableObject globalScope;

    public JsFilterLoader(
            Environment environment,
            LoggerBuilder loggerBuilder,
            JsonConfiguration configuration,
            EventQueue eventQueue,
            ExternalRequester externalRequester,
            HibernateSessionFactory sessionFactory,
            MatchersRegistry matchersRegistry) {

        this.environment = environment;
        this.logger = loggerBuilder.build(JsFilterLoader.class);
        this.loggerBuilder = loggerBuilder;
        this.eventQueue = eventQueue;
        this.externalRequester = externalRequester;
        this.sessionFactory = sessionFactory;
        this.matchersRegistry = matchersRegistry;
        logger.info("JsFilter LOADED");
        this.configuration = configuration;
    }

    protected Scriptable getNewScope(Context cx) {
        // global scope lazy initialization
        if (globalScope == null) {
            globalScope = cx.initStandardObjects();
        }

        return cx.newObject(globalScope);
    }

    public List<FilterDescriptor> loadFilters() {
        var result = new ArrayList<FilterDescriptor>();
        List<DbFilter> dbFilters = new ArrayList<>();
        // https://parsiya.net/blog/2019-12-22-using-mozilla-rhino-to-run-javascript-in-java/
        try {
            sessionFactory.query(em -> {
                var query = em.createQuery("SELECT e FROM DbFilter e " +
                        " ORDER BY e.priority DESC");
                dbFilters.addAll(query.getResultList());
            });
            for (var dbFilter : dbFilters) {
                loadSinglePlugin(result, dbFilter);
            }
        } catch (Exception e) {
            logger.error("Error compiling js filter ", e);
        }
        return result;
    }

    private void loadSinglePlugin(ArrayList<FilterDescriptor> result, DbFilter dbFilter) throws Exception {
        //if(dbFilter.getMatcherType().equalsIgnoreCase("http")){
        var filterDescriptor = new JsHttpFilterDescriptor();
        var serializedMatchers = mapper.readValue(dbFilter.getMatcher(), typeRef);
        List<FilterMatcher> matchers = new ArrayList<>();
        for (var serialized : serializedMatchers.entrySet()) {
            var fm = (FilterMatcher) mapper.readValue(serialized.getValue(), matchersRegistry.get(serialized.getKey()));
            if (fm instanceof ScriptMatcher) {
                initializeScriptMatcher((ScriptMatcher) fm);
            }
            matchers.add(fm);
        }

        sessionFactory.query(em -> {
            var query = em.createQuery("SELECT e.content FROM DbFilterRequire e WHERE" +
                    " e.binary=false" +
                    " AND e.scriptId=" + dbFilter.getId() +
                    " ORDER BY e.id DESC");
            filterDescriptor.getRequires().addAll(query.getResultList());
        });
        var src = dbFilter.getSource();

        var action = new JsHttpAction();
        action.setSource(src);
        action.setType(dbFilter.getType());

        filterDescriptor.setAction(action);
        filterDescriptor.setPhase(dbFilter.getPhase());
        if (dbFilter.getType().equalsIgnoreCase("script")) {
            precompileFilter(filterDescriptor);
        }
        var executor =
                new JsFilterExecutor(filterDescriptor, this, loggerBuilder, filterDescriptor.getId(),
                        matchers.toArray(new FilterMatcher[]{}));
        var fd = new FilterDescriptor(this, executor, environment, configuration);
        result.add(fd);
        //}
    }

    @Override
    public FilterDescriptor loadFilterFile(String fileName, byte[] fileData, boolean overwrite) {
        try {
            DbFilter jsonDbFilter = null;
            List<DbFilterRequire> requires = new ArrayList<>();
            var input = new ByteArrayInputStream(fileData);
            ZipInputStream zis = new ZipInputStream(input);
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                var path = Path.of(zipEntry.toString());
                var name = zipEntry.toString().toLowerCase();
                if (path.getParent() == null
                        && !zipEntry.isDirectory()
                        && name.endsWith("main.json")) {
                    jsonDbFilter = mapper.readValue(toString(zis), DbFilter.class);
                    if (jsonDbFilter.getName() == null || jsonDbFilter.getName().isEmpty()) {
                        jsonDbFilter.setName(fileName);
                    }
                } else if (path.getParent() != null && path.getParent().toString().endsWith("bin")) {
                    var binFileName = path.getFileName().toString();
                    var dbFilterRequire = new DbFilterRequire();
                    dbFilterRequire.setContent(Base64.encodeBase64String(toBytes(zis)));
                    dbFilterRequire.setBinary(true);
                    dbFilterRequire.setName(binFileName);
                    requires.add(dbFilterRequire);
                } else if (path.getParent() != null && path.getParent().toString().endsWith("txt")) {
                    var binFileName = path.getFileName().toString();
                    var dbFilterRequire = new DbFilterRequire();
                    dbFilterRequire.setContent(toString(zis));
                    dbFilterRequire.setBinary(false);
                    dbFilterRequire.setName(binFileName);
                    requires.add(dbFilterRequire);
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();

            final DbFilter forTrans = jsonDbFilter;
            sessionFactory.transactional(em -> {
                em.persist(forTrans);
                var id = forTrans.getId();
                for (var require : requires) {
                    require.setScriptId(id);
                    em.persist(require);
                }
            });

            if (jsonDbFilter != null) {
                var result = new ArrayList<FilterDescriptor>();
                loadSinglePlugin(result, forTrans);
                return result.get(0);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String toString(ZipInputStream zis) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = zis.read(buffer, 0, buffer.length)) > 0)
            baos.write(buffer, 0, read);
        return baos.toString(UTF_8.name());
    }

    private byte[] toBytes(ZipInputStream zis) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = zis.read(buffer, 0, buffer.length)) > 0)
            baos.write(buffer, 0, read);
        return baos.toByteArray();
    }


    private void initializeScriptMatcher(ScriptMatcher fm) throws Exception {
        StringBuilder scriptSrc =
                new StringBuilder(
                        "var globalFilterResult=runFilter(REQUESTJSON,utils);\n"
                                + "globalResult.put('continue', globalFilterResult);\n");
        scriptSrc.append("\r\nfunction runFilter(request,response,utils){");
        if (fm.getScript() != null) {
            scriptSrc
                    .append("\r\n")
                    .append(fm.getScript());
        }
        scriptSrc.append("\r\n}");
        Context cx = Context.enter();
        cx.setOptimizationLevel(9);
        cx.setLanguageVersion(Context.VERSION_1_8);
        //cx.setClassShutter(sandboxClassShutter);
        fm.initializeUtils(new JsUtils(this.sessionFactory, eventQueue, externalRequester));
        try {
            Scriptable currentScope = getNewScope(cx);
            fm.intializeScript(cx.compileString(scriptSrc.toString(), "my_script_id", 1, null));
        } catch (Exception ex) {
            logger.error("Error compiling script");
            logger.error(scriptSrc.toString());
            throw new Exception(ex);
        } finally {
            Context.exit();
        }
    }

    private void precompileFilter(JsHttpFilterDescriptor filterDescriptor) throws Exception {
        StringBuilder scriptSrc =
                new StringBuilder(
                        "var globalFilterResult=runFilter(REQUESTJSON,RESPONSEJSON,utils);\n"
                                + "globalResult.put('continue', globalFilterResult);\n");
        // Load all scripts
        if (filterDescriptor.getRequires() != null) {
            for (var file : filterDescriptor.getRequires()) {
                scriptSrc
                        .append("\r\n")
                        .append(file);
            }
        }
        scriptSrc.append("\r\nfunction runFilter(request,response,utils){");
        if (filterDescriptor.getAction().getSource() != null) {
            scriptSrc
                    .append("\r\n")
                    .append(filterDescriptor.getAction().getSource());
        }
        scriptSrc.append("\r\n}");

        Context cx = Context.enter();
        cx.setOptimizationLevel(9);
        cx.setLanguageVersion(Context.VERSION_1_8);
        //cx.setClassShutter(sandboxClassShutter);
        filterDescriptor.initializeQueue(new JsUtils(this.sessionFactory, eventQueue, externalRequester));
        try {
            Scriptable currentScope = getNewScope(cx);
            filterDescriptor.setScript(cx.compileString(scriptSrc.toString(), "my_script_id", 1, null));
        } catch (Exception ex) {
            logger.error("Error compiling script");
            logger.error(scriptSrc.toString());
            throw new Exception(ex);
        } finally {
            Context.exit();
        }
    }

    public static class SandboxClassShutter implements ClassShutter {
        public boolean visibleToScripts(String fullClassName) {
            return fullClassName.equals(HashMap.class.getName());
        }
    }
}
