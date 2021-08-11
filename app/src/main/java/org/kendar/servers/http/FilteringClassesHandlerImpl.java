package org.kendar.servers.http;

import org.apache.http.conn.HttpClientConnectionManager;
import org.kendar.http.*;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Component
public class FilteringClassesHandlerImpl implements FilteringClassesHandler {
    private List<CustomFilters> customFilterLoaders;
    private Environment environment;
    private HashMap<HttpFilterType,ArrayList<FilterDescriptor>> filters = new HashMap<>();
    class PrioritySorter implements Comparator<FilterDescriptor>
    {
        @Override
        public int compare(FilterDescriptor o1, FilterDescriptor o2) {
            return Integer.compare(o1.getPriority(),o2.getPriority());
        }
    }

    public FilteringClassesHandlerImpl(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
                                               List<CustomFilters> customFilterLoaders,Environment environment){

        this.customFilterLoaders = customFilterLoaders;
        this.environment = environment;
    }



    @PostConstruct
    public void init(){

        for(var filterLoader: customFilterLoaders){
            for (var ds : filterLoader.loadFilters()) {
                if(!filters.containsKey(ds.getPhase())){
                    filters.put(ds.getPhase(),new ArrayList<>());
                }
                filters.get(ds.getPhase()).add(ds);
            }
        }


        var prioritySorter = new PrioritySorter();
        for (var filterList :
                filters.entrySet()) {
            filterList.getValue().sort(prioritySorter);
        }
    }

    @Override
    public boolean handle(HttpFilterType filterType, Request request, Response response, HttpClientConnectionManager connectionManager) throws InvocationTargetException, IllegalAccessException {
        if(!filters.containsKey(filterType)) return false;
        for(var filterEntry: filters.get(filterType)){
            if(!methodMatches(filterEntry,request))continue;
            if(!filterMathches(filterEntry,request))continue;
            var isBlocking = filterEntry.execute(request,response,connectionManager);

            //If is blocking "by result"
            if(isBlocking == true){
                return true;
            }
            if(filterEntry.isBlocking()){
                return true;
            }
        }
        return false;
    }

    private boolean methodMatches(FilterDescriptor filterEntry, Request request) {
        if(filterEntry.getMethod().equalsIgnoreCase("*"))return true;
        return filterEntry.getMethod().equalsIgnoreCase(request.getMethod());
    }

    private boolean filterMathches(FilterDescriptor filterEntry, Request request) {
        if(!filterEntry.matchesHost(request.getHost(),environment))return false;
        return filterEntry.matchesPath(request.getPath(),environment,request);
    }
}
