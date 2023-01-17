package org.kendar.ham;

import java.util.HashMap;
import java.util.List;

public interface JsBuilder {

    public class ApiMatcher{
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
    public class FilterDescriptor{

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


        public void setType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setMatchers(HashMap<String, String> matchers) {
            this.matchers = matchers;
        }

        public HashMap<String, String> getMatchers() {
            return matchers;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * /api/plugins/jsfilter/filters
     * @return
     */
    List<String> filterIds() throws HamException;
    FilterDescriptor filterById(String id) throws HamException;
    JsFilterBuilder addFilter(String id);
    void deleteFilter(String id) throws HamException;

    JsBuilderImpl init();
}
