package org.kendar.servers.http.matchers;

import java.util.List;

public interface MatchersRegistry {
    Class<?> get(String key);
    List<String> getAll();
}
