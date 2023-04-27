package org.kendar.ham;

import java.util.HashMap;
import java.util.List;

public interface JsBuilder {

    /**
     * /api/plugins/jsfilter/filters
     *
     * @return
     */
    List<String> filterIds() throws HamException;

    FilterDescriptor filterById(Long id) throws HamException;

    JsFilterBuilder addFilter(String name);

    void deleteFilter(Long id) throws HamException;

    JsBuilderImpl init();

    public class ApiMatcher implements Matcher {
        private Methods method;
        private String hostAddress;
        private String hostPattern;
        private String pathAddress;
        private String pathPattern;

        public Methods getMethod() {
            return method;
        }

        public void setMethod(Methods method) {
            this.method = method;
        }

        public String getHostAddress() {
            return hostAddress;
        }

        public void setHostAddress(String hostAddress) {
            this.hostAddress = hostAddress;
        }

        public String getHostPattern() {
            return hostPattern;
        }

        public void setHostPattern(String hostPattern) {
            this.hostPattern = hostPattern;
        }

        public String getPathAddress() {
            return pathAddress;
        }

        public void setPathAddress(String pathAddress) {
            this.pathAddress = pathAddress;
        }

        public String getPathPattern() {
            return pathPattern;
        }

        public void setPathPattern(String pathPattern) {
            this.pathPattern = pathPattern;
        }
    }

    public class FilterDescriptor {

        private FilterPhase phase;
        private List<String> require;
        private int priority;
        private boolean blocking;
        private String source;
        private String type;
        private HashMap<String, String> matchers;
        private String name;

        public FilterPhase getPhase() {
            return phase;
        }

        public void setPhase(FilterPhase phase) {
            this.phase = phase;
        }


        public List<String> getRequire() {
            return require;
        }

        public void setRequire(List<String> require) {
            this.require = require;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public boolean isBlocking() {
            return blocking;
        }

        public void setBlocking(boolean blocking) {
            this.blocking = blocking;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public HashMap<String, String> getMatchers() {
            return matchers;
        }

        public void setMatchers(HashMap<String, String> matchers) {
            this.matchers = matchers;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public class ScriptMatcher implements Matcher {
        private String script;
        private String hostAddress;
        private String pathAddress;

        public String getScript() {
            return script;
        }

        public void setScript(String script) {
            this.script = script;
        }

        public String getHostAddress() {
            return hostAddress;
        }

        public void setHostAddress(String hostAddress) {
            this.hostAddress = hostAddress;
        }

        public String getPathAddress() {
            return pathAddress;
        }

        public void setPathAddress(String pathAddress) {
            this.pathAddress = pathAddress;
        }
    }
}
