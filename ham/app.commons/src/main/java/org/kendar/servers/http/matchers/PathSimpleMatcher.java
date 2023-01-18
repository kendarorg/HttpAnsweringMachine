package org.kendar.servers.http.matchers;

import org.kendar.servers.http.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class PathSimpleMatcher {

    private List<String> pathSimpleMatchers = new ArrayList<>();

    public void setupPathSimpleMatchers(String pathAddress) {
        pathSimpleMatchers = new ArrayList<String>();
        if (pathAddress!=null && pathAddress.contains("{")) {
            var explTemplate = pathAddress.split("/");
            for (var i = 0; i < explTemplate.length; i++) {
                var partTemplate = explTemplate[i];
                if (partTemplate.startsWith("{")) {
                    partTemplate = partTemplate.substring(1);
                    partTemplate = "*" + partTemplate.substring(0, partTemplate.length() - 1);
                }
                pathSimpleMatchers.add(partTemplate);
            }
        }
    }

    public boolean notMatch(String real, String provided) {
        if(provided==null)return false;
        if(provided.equalsIgnoreCase("*"))return false;
        if(real.equalsIgnoreCase(provided))return false;
        return true;
    }

    public boolean matches(Request req){
        if (pathSimpleMatchers!=null && pathSimpleMatchers.size() > 0) {
            var explPath = req.getPath().split("/");
            if (pathSimpleMatchers.size() != explPath.length) return false;
            for (var i = 0; i < pathSimpleMatchers.size(); i++) {
                var partTemplate = pathSimpleMatchers.get(i);
                var partPath = explPath[i];
                if (partTemplate.startsWith("*")) {
                    partTemplate = partTemplate.substring(1);
                    req.addPathParameter(partTemplate, partPath);
                } else if (!partTemplate.equalsIgnoreCase(partPath)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
