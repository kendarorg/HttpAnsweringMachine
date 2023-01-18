package org.kendar.servers.http.matchers;

import org.kendar.servers.http.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class PathRegexpMatcher {
    private static final Pattern namedGroupsPattern =
            Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z\\d]*)>");
    private List<String> pathMatchers;
    public void getNamedGroupCandidates(String pathPattern) {
        Set<String> matchedGroups = new TreeSet<>();
        var m = namedGroupsPattern.matcher(pathPattern);
        while (m.find()) {
            matchedGroups.add(m.group(1));
        }
        pathMatchers = new ArrayList<>(matchedGroups);
    }

    public boolean matches(Request req, Pattern pathPatternReal){
        if (pathPatternReal != null) {
            var matcher = pathPatternReal.matcher(req.getPath());
            if (matcher.matches()) {
                for (int i = 0; i < pathMatchers.size(); i++) {
                    var group = matcher.group(pathMatchers.get(i));
                    if (group != null) {
                        req.addPathParameter(pathMatchers.get(i), group);
                    }
                }
                return true;
            }
        }
        return false;
    }
}
