package org.kendar.http;

import org.apache.commons.lang3.ClassUtils;
import org.apache.http.conn.HttpClientConnectionManager;
import org.kendar.http.annotations.*;
import org.kendar.http.annotations.concrete.HamDocConcrete;
import org.kendar.http.annotations.concrete.HttpMethodFilterConcrete;
import org.kendar.http.annotations.concrete.HttpTypeFilterConcrete;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.servers.http.matchers.ApiMatcher;
import org.kendar.servers.http.matchers.FilterMatcher;
import org.springframework.core.env.Environment;

import java.lang.annotation.IncompleteAnnotationException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class FilterDescriptor {

    private static final Pattern namedGroupsPattern =
            Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z\\d]*)>");
    private final int priority;
    private final boolean methodBlocking;
    private final boolean typeBlocking;
    private final Object filterClass;
    private final List<String> pathSimpleMatchers = new ArrayList<>();
    private final JsonConfiguration jsonConfiguration;
    private final CustomFiltersLoader loader;

    public List<FilterMatcher> getMatchers() {
        return matchers;
    }

    private List<FilterMatcher> matchers = new ArrayList<>();
    private HamMatcher[] extraMatches;
    private String description;
    private HttpFilterType phase;
    private HttpTypeFilter typeFilter;
    private HttpMethodFilter methodFilter;

    private HamDoc doc;
    private Method callback;
    private List<String> pathMatchers = new ArrayList<>();
    private String id;

    public FilterDescriptor(
            CustomFiltersLoader loader,
            HttpTypeFilter typeFilter,
            HttpMethodFilter methodFilter,
            Method callback,
            FilteringClass filterClass,
            Environment environment,
            JsonConfiguration jsonConfiguration,
            HamDoc hamDoc) {
        this.doc = hamDoc;
        this.loader = loader;
        this.id = IdBuilder.buildId(typeFilter, methodFilter, filterClass);
        this.description = methodFilter.description();
        this.callback = callback;
        this.filterClass = filterClass;
        this.jsonConfiguration = jsonConfiguration;
        try {
            this.id = IdBuilder.buildId(typeFilter, methodFilter, filterClass);
        } catch (IncompleteAnnotationException ex) {
            throw new RuntimeException("Missing id", ex);
        }
        var matcher = new ApiMatcher(typeFilter.hostAddress(), typeFilter.hostPattern(),
                methodFilter.method(), methodFilter.pathPattern(), methodFilter.pathAddress());
        matcher.initialize(in -> getWithEnv(in, environment));
        priority = typeFilter.priority();
        phase = methodFilter.phase();
        methodBlocking = methodFilter.blocking();
        typeBlocking = typeFilter.blocking();
        matchers.add(matcher);
        this.extraMatches = methodFilter.matcher();
        this.typeFilter = buildTypeFilter();
        this.methodFilter = buildMethodFilter();
    }

    public FilterDescriptor(
            CustomFiltersLoader loader,
            GenericFilterExecutor executor,
            Environment environment,
            JsonConfiguration jsonConfiguration) {
        this.loader = loader;
        this.jsonConfiguration = jsonConfiguration;

        for (var method : executor.getClass().getMethods()) {
            if (method.getName().equalsIgnoreCase("run")) {
                this.callback = method;
                break;
            }
        }
        this.filterClass = executor;
        this.id = executor.getId();
        for (var matcher : executor.getMatchers()) {
            matcher.initialize(in -> getWithEnv(in, environment));
            if (!matcher.validate()) {
                throw new RuntimeException("Invalid filter");
            }
        }
        matchers = executor.getMatchers();

        priority = executor.getPriority();
        phase = executor.getPhase();
        methodBlocking = executor.isMethodBlocking();
        typeBlocking = executor.isTypeBlocking();
        this.typeFilter = buildTypeFilter();
        this.methodFilter = buildMethodFilter();
    }

    private static List<String> getNamedGroupCandidates(String regex) {
        Set<String> matchedGroups = new TreeSet<>();
        var m = namedGroupsPattern.matcher(regex);
        while (m.find()) {
            matchedGroups.add(m.group(1));
        }
        return new ArrayList<>(matchedGroups);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassId() {
        return filterClass.getClass().getName();
    }

    private HttpMethodFilter buildMethodFilter() {
        var matcherUnknown = matchers.get(0);
        if (ClassUtils.isAssignable(matcherUnknown.getClass(), ApiMatcher.class)) {
            var matcher = (ApiMatcher) matcherUnknown;
            return new HttpMethodFilterConcrete(phase, methodBlocking,
                    matcher.getPathAddress(), matcher.getPathPatternReal(), matcher.getMethod(),
                    description,
                    id, extraMatches);
        } else {
            return new HttpMethodFilterConcrete(phase, methodBlocking,
                    "*", null, "*",
                    description,
                    id, extraMatches);
        }
    }

    private HttpTypeFilter buildTypeFilter() {
        var matcherUnknown = matchers.get(0);
        if (ClassUtils.isAssignable(matcherUnknown.getClass(), ApiMatcher.class)) {
            var matcher = (ApiMatcher) matcherUnknown;
            return new HttpTypeFilterConcrete(matcher.getHostAddress(), typeBlocking, priority, matcher.getHostPatternReal());
        } else {
            return new HttpTypeFilterConcrete("*", typeBlocking, priority, null);
        }
    }

    public int getPriority() {
        return priority;
    }

    public String getWithEnv(String data, Environment env) {
        if (data.startsWith("${") && data.endsWith("}")) {
            var hostVar = data.substring(0, data.length() - 1).substring(2);
            var defaultVar = hostVar.split(":", 2);
            data = env.getProperty(defaultVar[0]);
            if (data == null) {
                data = jsonConfiguration.getValue(defaultVar[0]);
            }
            if (data == null && defaultVar.length == 2) {
                data = defaultVar[1];
            }
        }
        return data;
    }

    public boolean matches(Request req) {
        for (var match : matchers) {
            if (!match.matches(req)) return false;
        }
        return true;
    }

    public boolean execute(
            Request request, Response response, HttpClientConnectionManager connectionManager)
            throws InvocationTargetException, IllegalAccessException {
        Object result = null;
        if (callback.getParameterCount() == 3) {
            result = callback.invoke(filterClass, request, response, connectionManager);
        } else if (callback.getParameterCount() == 2) {
            result = callback.invoke(filterClass, request, response);
        } else if (callback.getParameterCount() == 1) {
            result = callback.invoke(filterClass, request);
        } else if (callback.getParameterCount() == 0) {
            result = callback.invoke(filterClass);
        }
        if (callback.getReturnType() == boolean.class) {
            if (result == null) {
                result = false;
            }
            return (boolean) result;
        }
        return false;
    }

    public boolean isBlocking() {
        if (this.phase == HttpFilterType.API) {
            return true;
        }
        return this.methodBlocking || this.typeBlocking;
    }

    public HttpFilterType getPhase() {
        return phase;
    }

    public void setPhase(HttpFilterType phase) {
        this.phase = phase;
    }

    public HttpTypeFilter getTypeFilter() {
        return typeFilter;
    }

    public void setTypeFilter(HttpTypeFilter typeFilter) {
        this.typeFilter = typeFilter;
    }

    public HttpMethodFilter getMethodFilter() {
        return methodFilter;
    }

    public void setMethodFilter(HttpMethodFilter methodFilter) {
        this.methodFilter = methodFilter;
    }

    public CustomFiltersLoader getLoader() {
        return loader;
    }

    public HamDoc getHamDoc() {

        var loc = this;
        if (this.doc == null) return null;
        return new HamDocConcrete(doc);
    }
}
