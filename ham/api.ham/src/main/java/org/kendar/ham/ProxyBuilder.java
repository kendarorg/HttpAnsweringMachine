package org.kendar.ham;

import java.util.List;

public interface ProxyBuilder {
    public class Proxy{
        public String id;
        public String when;
        public String where;
        public String test;
    }

    String addProxy(String when,String where, String test) throws HamException;
    void removeProxy(String id);
    List<Proxy> retrieveProxies() throws HamException;
    void refresh();
}
