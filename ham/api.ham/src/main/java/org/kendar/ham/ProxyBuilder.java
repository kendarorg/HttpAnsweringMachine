package org.kendar.ham;

import java.util.List;

/**
 * Builder for the proxy functions
 */
public interface ProxyBuilder {
    public class Proxy{
        private String id;
        private String when;
        private String where;
        private String test;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getWhen() {
            return when;
        }

        public void setWhen(String when) {
            this.when = when;
        }

        public String getWhere() {
            return where;
        }

        public void setWhere(String where) {
            this.where = where;
        }

        public String getTest() {
            return test;
        }

        public void setTest(String test) {
            this.test = test;
        }
    }

    /**
     * Add a proxy
     * @param when the matching "address bar"
     * @param where the when will be replaced with where
     * @param test the domain/domain:port that will be inspected to check. If it's running the proxy will be used
     * @return the id of the added proxy
     * @throws HamException
     */
    String addProxy(String when,String where, String test) throws HamException;

    /**
     * Remove proxy by id
     * @param id
     * @throws HamException
     */
    void removeProxy(String id) throws HamException;

    /**
     * List all current proxyes with their states
     * @return
     * @throws HamException
     */
    List<Proxy> retrieveProxies() throws HamException;

    /**
     * Refresh proxy
     * @throws HamException
     */
    void refresh() throws HamException;
}
