package org.kendar.servers.http.matchers;

import org.kendar.servers.http.Request;

import java.util.function.Function;

public interface FilterMatcher {
    boolean matches(Request req);

    void initialize(Function<String, String> apply);

    boolean validate();
}
