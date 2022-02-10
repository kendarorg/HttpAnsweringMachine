package org.kendar.servers.http;

import org.apache.http.conn.HttpClientConnectionManager;
import org.kendar.events.EventQueue;
import org.kendar.http.*;
import org.kendar.http.events.ScriptsModified;
import org.kendar.servers.config.GlobalConfig;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Component
public class FilteringClassesHandlerImpl implements FilteringClassesHandler {
  private final List<CustomFiltersLoader> customFilterLoaders;
  private final Environment environment;
  private final FilterConfig filtersConfiguration;
  private final Logger logger;

  public FilteringClassesHandlerImpl(
          List<CustomFiltersLoader> customFilterLoaders,
          Environment environment,
          FilterConfig filtersConfiguration,
          LoggerBuilder loggerBuilder,
          EventQueue eventQueue) {
    this.customFilterLoaders = customFilterLoaders;
    this.environment = environment;
    this.filtersConfiguration = filtersConfiguration;
    this.logger = loggerBuilder.build(FilteringClassesHandlerImpl.class);

    eventQueue.register(this::handleScriptModified, ScriptsModified.class);
  }

  public HashMap<HttpFilterType, List<FilterDescriptor>> getConfiguration() {
    return filtersConfiguration.get().filters;
  }

  public void setConfiguration(HashMap<HttpFilterType, List<FilterDescriptor>> config) {
    // filtersConfiguration.set(config);
  }

  private void handleScriptModified(ScriptsModified event){
    init();
  }

  @PostConstruct
  public void init() {
    var config = new FiltersConfiguration();
    config.filters.put(HttpFilterType.NONE, new ArrayList<>());
    config.filters.put(HttpFilterType.PRE_RENDER, new ArrayList<>());
    config.filters.put(HttpFilterType.API, new ArrayList<>());
    config.filters.put(HttpFilterType.STATIC, new ArrayList<>());
    config.filters.put(HttpFilterType.PRE_CALL, new ArrayList<>());
    config.filters.put(HttpFilterType.POST_CALL, new ArrayList<>());
    config.filters.put(HttpFilterType.POST_RENDER, new ArrayList<>());

    var duplicateIds = new HashSet<>();
    for (var filterLoader : customFilterLoaders) {
      for (var ds : filterLoader.loadFilters()) {
        config.filters.get(ds.getPhase()).add(ds);
        if (ds.getId().equalsIgnoreCase("null")) {
          ds.setId("null:" + UUID.randomUUID());
        }
        var id = ds.getId();
        if (duplicateIds.contains(id)) {
          throw new RuntimeException("Duplicate filter id " + id);
        }
        duplicateIds.add(id);
        config.filtersById.put(id, ds);
        if (!config.filtersByClass.containsKey(ds.getClassId())) {
          config.filtersByClass.put(ds.getClassId(), new ArrayList<>());
        }
        config.filtersByClass.get(ds.getClassId()).add(ds);
      }
    }
    filtersConfiguration.set(config);
  }

  @Override
  public boolean handle(
      GlobalConfig globalConfig,
      HttpFilterType filterType,
      Request request,
      Response response,
      HttpClientConnectionManager connectionManager)
      throws InvocationTargetException, IllegalAccessException {
    var config = filtersConfiguration.get();
    if (!config.filters.containsKey(filterType)) return false;
    var possibleMatches = new ArrayList<FilterDescriptor>();
    for (var filterEntry : config.filters.get(filterType)) {
      if (!methodMatches(filterEntry, request)) continue;
      if (!filterMathches(filterEntry, request)) continue;
      if (!globalConfig.checkFilterEnabled(filterEntry.getId())
              || !globalConfig.checkFilterEnabled(filterEntry.getClassId())) {
        logger.debug(
                "Filter {} with id {} is disabled", filterEntry.getClassId(), filterEntry.getId());
        continue;
      }
      possibleMatches.add(filterEntry);
    }
    for (var filterEntry : possibleMatches) {

        var isBlocking = filterEntry.execute(request, response, connectionManager);
        // If is blocking "by result"
        if (isBlocking) {
          return true;
        }
        if (filterEntry.isBlocking()) {
          return true;
        }
    }
    return false;
  }

  private boolean methodMatches(FilterDescriptor filterEntry, Request request) {
    if (filterEntry.getMethod().equalsIgnoreCase("*")) return true;
    return filterEntry.getMethod().equalsIgnoreCase(request.getMethod());
  }

  private boolean filterMathches(FilterDescriptor filterEntry, Request request) {
    if (!filterEntry.matchesHost(request.getHost(), environment)) return false;
    return filterEntry.matchesPath(request.getPath(), environment, request,false);
  }

  private boolean filterMatchesExact(FilterDescriptor filterEntry, Request request) {
    return filterEntry.matchesPath(request.getPath(), environment, request,false);
  }

  static class PrioritySorter implements Comparator<FilterDescriptor> {
    @Override
    public int compare(FilterDescriptor o1, FilterDescriptor o2) {
      return Integer.compare(o1.getPriority(), o2.getPriority());
    }
  }
}
