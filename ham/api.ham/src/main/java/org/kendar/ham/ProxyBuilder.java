package org.kendar.ham;

import java.util.List;

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

    String addProxy(String when,String where, String test) throws HamException;
    void removeProxy(String id) throws HamException;
    List<Proxy> retrieveProxies() throws HamException;
    void refresh() throws HamException;
}
