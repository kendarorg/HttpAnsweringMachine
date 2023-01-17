package org.kendar.replayer.matcher;

import org.kendar.servers.http.Request;
import org.kendar.servers.http.matchers.FilterMatcher;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class QueryMatcher implements FilterMatcher {
    @Override
    public boolean matches(Request req) {
        return false;
    }

    @Override
    public void initialize(Function<String, String> apply) {

    }
}
