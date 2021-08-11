package org.kendar.servers.http;

import org.kendar.http.CustomFilters;
import org.kendar.http.FilterDescriptor;
import org.kendar.http.FilteringClass;
import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultFiltersLoader implements CustomFilters {
    private final Logger logger;
    private List<FilteringClass> filteringClassList;
    private Environment environment;

    public DefaultFiltersLoader(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
                                        List<FilteringClass> filteringClassList,
                                Environment environment, LoggerBuilder loggerBuilder){

        this.filteringClassList = filteringClassList;
        this.environment = environment;
        logger = loggerBuilder.build(DefaultFiltersLoader.class);
    }

    private static boolean hasFilterType(FilteringClass cl){
        return cl.getClass().getAnnotation(HttpTypeFilter.class)!=null;
    }

    private static List<FilterDescriptor> getAnnotatedMethods(FilteringClass cl,Environment environment) {
        var result = new ArrayList<FilterDescriptor>();
        var typeFilter = cl.getClass().getAnnotation(HttpTypeFilter.class);
        for (Method m: cl.getClass().getMethods()) {
            var methodFilter = m.getAnnotation(HttpMethodFilter.class);
            if(methodFilter==null) continue;
            result.add(new FilterDescriptor(typeFilter,methodFilter,m,cl,environment));
        }
        return result;
    }
    @Override
    public List<FilterDescriptor> loadFilters() {
        var result = new ArrayList<FilterDescriptor>();
        for (var filterClass :filteringClassList) {
            if(!hasFilterType(filterClass)) continue;
            for (FilterDescriptor ds: getAnnotatedMethods(filterClass,environment)) {
                result.add(ds);
            }
        }

        logger.info("Standard filters LOADED");
        return result;
    }
}
