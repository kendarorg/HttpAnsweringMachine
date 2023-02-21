package org.kendar.servers.http;

import org.apache.http.conn.HttpClientConnectionManager;
import org.kendar.events.EventQueue;
import org.kendar.http.*;
import org.kendar.http.events.ScriptsModified;
import org.kendar.servers.WaitForService;
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
  private WaitForService waitForService;

  public FilteringClassesHandlerImpl(
          List<CustomFiltersLoader> customFilterLoaders,
          Environment environment,
          FilterConfig filtersConfiguration,
          LoggerBuilder loggerBuilder,
          EventQueue eventQueue,
          WaitForService waitForService) {
    this.customFilterLoaders = customFilterLoaders;
    this.environment = environment;
    this.filtersConfiguration = filtersConfiguration;
    this.logger = loggerBuilder.build(FilteringClassesHandlerImpl.class);
    this.waitForService = waitForService;

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
    this.waitForService.waitForService("db",()->{
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
          if (ds.getId()==null || ds.getId().equalsIgnoreCase("null")) {
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
      for(var kvp:config.filters.entrySet()){
        var list = kvp.getValue();
        Collections.sort(list,Comparator.comparingInt(FilterDescriptor::getPriority).reversed());
      }
      filtersConfiguration.set(config);
    });

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
    if(config==null)return false;
    if (!config.filters.containsKey(filterType)) return false;
    var possibleMatches = new ArrayList<FilterDescriptor>();
    for (var filterEntry : config.filters.get(filterType)) {
      if(!filterEntry.matches(request))continue;
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

  static class PrioritySorter implements Comparator<FilterDescriptor> {
    @Override
    public int compare(FilterDescriptor o1, FilterDescriptor o2) {
      return Integer.compare(o1.getPriority(), o2.getPriority());
    }
  }
}
