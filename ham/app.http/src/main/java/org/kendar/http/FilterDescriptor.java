package org.kendar.http;

import org.apache.http.conn.HttpClientConnectionManager;
import org.kendar.http.annotations.*;
import org.kendar.http.annotations.concrete.HamDocConcrete;
import org.kendar.http.annotations.concrete.HamMatcherConcrete;
import org.kendar.http.annotations.concrete.HttpMethodFilterConcrete;
import org.kendar.http.annotations.concrete.HttpTypeFilterConcrete;
import org.kendar.http.annotations.multi.*;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.core.env.Environment;

import java.lang.annotation.Annotation;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

public class FilterDescriptor {

  private static final Pattern namedGroupsPattern =
      Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z\\d]*)>");
  private final int priority;
  private final String method;
  private final boolean methodBlocking;
  private final boolean typeBlocking;
  private final Object filterClass;
  private final List<String> pathSimpleMatchers = new ArrayList<>();
  private final JsonConfiguration jsonConfiguration;
  private final CustomFiltersLoader loader;
  private HamMatcher[] extraMatches;
  private String description;
  private String hostAddress;
  private String pathAddress;
  private Pattern hostPattern;
  private Pattern pathPattern;
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
    this.id = IdBuilder.buildId(typeFilter,methodFilter,filterClass);
    this.description = methodFilter.description();
    this.callback = callback;
    this.filterClass = filterClass;
    this.jsonConfiguration = jsonConfiguration;
    try {
      this.id = IdBuilder.buildId(typeFilter,methodFilter,filterClass);
    } catch (IncompleteAnnotationException ex) {
      throw new RuntimeException("Missing id", ex);
    }
    if (typeFilter.hostPattern().length() > 0) {
      var realHostPattern = getWithEnv(typeFilter.hostPattern(), environment);
      hostPattern = Pattern.compile(realHostPattern);
    } else {
      hostAddress = getWithEnv(typeFilter.hostAddress(), environment);
    }
    priority = typeFilter.priority();
    method = methodFilter.method();
    phase = methodFilter.phase();
    methodBlocking = methodFilter.blocking();
    typeBlocking = typeFilter.blocking();
    if (methodFilter.pathPattern().length() > 0) {
      var realPathPattern = getWithEnv(methodFilter.pathPattern(), environment);
      pathPattern = Pattern.compile(realPathPattern);
      pathMatchers = getNamedGroupCandidates(realPathPattern);
    } else {
      pathAddress = getWithEnv(methodFilter.pathAddress(), environment);
      setupPathSimpleMatchers();
    }
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
    if (null != executor.getHostPattern() && executor.getHostPattern().length() > 0) {
      var realHostPattern = getWithEnv(executor.getHostPattern(), environment);
      hostPattern = Pattern.compile(realHostPattern);
    } else {
      hostAddress = getWithEnv(executor.getHostAddress(), environment);
    }
    priority = executor.getPriority();
    method = executor.getMethod();
    phase = executor.getPhase();
    methodBlocking = executor.isMethodBlocking();
    typeBlocking = executor.isTypeBlocking();
    if (null != executor.getPathPattern() && executor.getPathPattern().length() > 0) {
      var realPathPattern = getWithEnv(executor.getPathPattern(), environment);
      pathPattern = Pattern.compile(realPathPattern);
      pathMatchers = getNamedGroupCandidates(realPathPattern);
    } else {
      pathAddress = getWithEnv(executor.getPathAddress(), environment);
      setupPathSimpleMatchers();
    }
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
    var loc = this;
    return new HttpMethodFilterConcrete(phase, methodBlocking,
    pathAddress, pathPattern,
            method, description,
            id, extraMatches);
  }

  private HttpTypeFilter buildTypeFilter() {
    var loc = this;
    return new HttpTypeFilterConcrete(hostAddress,typeBlocking,priority,hostPattern);
  }

  private void setupPathSimpleMatchers() {
    if (pathAddress.contains("{")) {
      var explTemplate = pathAddress.split("/");
      for (var i = 0; i < explTemplate.length; i++) {
        var partTemplate = explTemplate[i];
        if (partTemplate.startsWith("{")) {
          partTemplate = partTemplate.substring(1);
          partTemplate = "*" + partTemplate.substring(0, partTemplate.length() - 1);
        }
        pathSimpleMatchers.add(partTemplate);
      }
    }
  }

  public int getPriority() {
    return priority;
  }

  public String getMethod() {
    return method;
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

  public boolean matchesHost(String host, Environment env) {
    if (hostAddress != null && hostAddress.equalsIgnoreCase("*")) return true;
    if (hostPattern != null) {
      return hostPattern.matcher(host).matches();
    }
    return host.equalsIgnoreCase(hostAddress);
  }

  public boolean matchesPath(String path, Environment env, Request request,boolean exact) {
    if(exact){
      if(pathAddress==null ||pathAddress.length()==0 ||pathAddress.indexOf('*')>=0){
        return false;
      }
      return path.equalsIgnoreCase(pathAddress);
    }
    if (pathAddress != null && pathAddress.equalsIgnoreCase("*")) return true;
    if (pathPattern != null) {
      var matcher = pathPattern.matcher(path);
      if (matcher.matches()) {
        for (int i = 0; i < pathMatchers.size(); i++) {
          var group = matcher.group(pathMatchers.get(i));
          if (group != null) {
            request.addPathParameter(pathMatchers.get(i), group);
          }
        }
        return true;
      }
    }

    if (pathSimpleMatchers.size() > 0) {
      var explPath = path.split("/");
      if (pathSimpleMatchers.size() != explPath.length) return false;
      for (var i = 0; i < pathSimpleMatchers.size(); i++) {
        var partTemplate = pathSimpleMatchers.get(i);
        var partPath = explPath[i];
        if (partTemplate.startsWith("*")) {
          partTemplate = partTemplate.substring(1);
          request.addPathParameter(partTemplate, partPath);
        } else if (!partTemplate.equalsIgnoreCase(partPath)) {
          return false;
        }
      }
      return true;
    }
    return path.equalsIgnoreCase(pathAddress);
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
      if(this.doc==null) return null;
      return new HamDocConcrete(doc);
    }
}
