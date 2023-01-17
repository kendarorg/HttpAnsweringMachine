package org.kendar.servers.http.matchers;

public interface MatchersRegistry {
    Class<?> get(String key);
}
