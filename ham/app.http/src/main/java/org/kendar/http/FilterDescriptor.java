package org.kendar.http;

import org.apache.http.conn.HttpClientConnectionManager;
import org.kendar.http.annotations.*;
import org.kendar.servers.JsonConfiguration;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.springframework.core.env.Environment;

import java.lang.annotation.Annotation;
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
      Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");
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
      JsonConfiguration jsonConfiguration) {
    this.loader = loader;
    this.id = methodFilter.id();
    this.description = methodFilter.description();
    this.callback = callback;
    this.filterClass = filterClass;
    this.jsonConfiguration = jsonConfiguration;
    try {
      this.id = methodFilter.id();
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
    return new HttpMethodFilter() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return null;
      }

      @Override
      public HttpFilterType phase() {
        return loc.phase;
      }

      @Override
      public boolean blocking() {
        return loc.methodBlocking;
      }

      @Override
      public String pathAddress() {
        return loc.pathAddress;
      }

      @Override
      public String pathPattern() {
        if (loc.pathPattern == null) return null;
        return loc.pathPattern.toString();
      }

      @Override
      public String method() {
        return loc.method;
      }

      @Override
      public String description() {
        return loc.description;
      }

      @Override
      public String id() {
        return loc.getId();
      }

      @Override
      public HamMatcher[] matcher() {
        var result = new ArrayList<HamMatcher>();
        if(loc.extraMatches!=null && loc.extraMatches.length>0){
          for (var matcher :
                  extraMatches) {
            result.add(new HamMatcher() {
              @Override
              public Class<? extends Annotation> annotationType() {
                return null;
              }

              @Override
              public String value() {
                return matcher.value();
              }

              @Override
              public MatcherFunction function() {
                return matcher.function();
              }

              @Override
              public MatcherType type() {
                return matcher.type();
              }

              @Override
              public String id() {
                if(matcher.id().length()>0){
                  return matcher.id();
                }
                return null;
              }
            });
          }
        }
        return result.toArray(new HamMatcher[0]);
      }
    };
  }

  private HttpTypeFilter buildTypeFilter() {
    var loc = this;
    return new HttpTypeFilter() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return null;
      }

      @Override
      public String hostAddress() {
        return loc.hostAddress;
      }

      @Override
      public String hostPattern() {
        if (loc.hostPattern == null) return null;
        return loc.hostPattern.toString();
      }

      @Override
      public String name() {
        return "";
      }

      @Override
      public int priority() {
        return loc.priority;
      }

      @Override
      public boolean blocking() {
        return loc.typeBlocking;
      }
    };
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
}
