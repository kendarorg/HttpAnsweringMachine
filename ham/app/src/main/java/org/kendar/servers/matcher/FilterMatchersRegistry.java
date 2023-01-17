package org.kendar.servers.matcher;

import org.checkerframework.checker.units.qual.A;
import org.kendar.servers.http.matchers.ApiMatcher;
import org.kendar.servers.http.matchers.FilterMatcher;
import org.kendar.servers.http.matchers.MatchersRegistry;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class FilterMatchersRegistry implements MatchersRegistry {
    private Map<String,Class<?>> matcherList = new HashMap<>();

    public FilterMatchersRegistry(List<FilterMatcher> matcherList){

        this.matcherList.put(ApiMatcher.class.getSimpleName().toLowerCase(Locale.ROOT),ApiMatcher.class);
        for(var m:matcherList){
            this.matcherList.put(m.getClass().getSimpleName().toLowerCase(Locale.ROOT),m.getClass());
        }
    }

    @Override
    public Class<?> get(String key) {
        return matcherList.get(key.toLowerCase(Locale.ROOT));
    }
}
