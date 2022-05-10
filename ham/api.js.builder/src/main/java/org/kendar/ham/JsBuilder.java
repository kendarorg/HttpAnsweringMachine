package org.kendar.ham;

import java.util.ArrayList;
import java.util.List;

public interface JsBuilder {

    public class FilterDescriptor{
        private Methods method;
        private String hostAddress;
        private String hostRegexp;
        private String pathAddress;
        private String pathRegexp;
        private FilterPhase phase;
        private String root;
        private List<String> requires;
        private int priority;
        private boolean blocking;
        private List<String> source = new ArrayList<>();
        private String id;

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

        public String getHostRegexp() {
            return hostRegexp;
        }

        public void setHostRegexp(String hostRegexp) {
            this.hostRegexp = hostRegexp;
        }

        public String getPathAddress() {
            return pathAddress;
        }

        public void setPathAddress(String pathAddress) {
            this.pathAddress = pathAddress;
        }

        public String getPathRegexp() {
            return pathRegexp;
        }

        public void setPathRegexp(String pathRegexp) {
            this.pathRegexp = pathRegexp;
        }

        public FilterPhase getPhase() {
            return phase;
        }

        public void setPhase(FilterPhase phase) {
            this.phase = phase;
        }

        public String getRoot() {
            return root;
        }

        public void setRoot(String root) {
            this.root = root;
        }

        public List<String> getRequires() {
            return requires;
        }

        public void setRequires(List<String> requires) {
            this.requires = requires;
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

        public List<String> getSource() {
            return source;
        }

        public void setSource(List<String> source) {
            this.source = source;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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
