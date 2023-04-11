package org.kendar.ham;

import org.kendar.servers.dbproxy.DbProxy;

import java.util.List;

/**
 * Builder for the proxy functions
 */
public interface ProxyBuilder {

    /**
     * Remove a local db proxy
     *
     * @param exposed the exposed dbname
     */
    void removeDbProxy(String exposed) throws HamException;

    public class Proxy {
        private String id;
        private String when;
        private String where;
        private String test;

        private boolean force;

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

        public boolean isForce() {
            return force;
        }

        public void setForce(boolean force) {
            this.force = force;
        }
    }

    /**
     * Add a proxy
     *
     * @param when  the matching "address bar"
     * @param where the when will be replaced with where
     * @param test  the domain/domain:port that will be inspected to check. If it's running the proxy will be used
     * @return the id of the added proxy
     * @throws HamException
     */
    String addProxy(String when, String where, String test) throws HamException;

    /**
     * Add a proxy
     *
     * @param when  the matching "address bar"
     * @param where the when will be replaced with where
     * @param test  the domain/domain:port that will be inspected to check. If it's running the proxy will be used
     * @return the id of the added proxy
     * @throws HamException
     */
    String addForcedProxy(String when, String where, String test) throws HamException;

    /**
     * Retrieve proxy by id
     * @param id
     * @return
     * @throws HamException
     */
    Proxy retrieveProxy(String id) throws HamException;

    /**
     * Add a db proxy, specify the db to be proxied
     *
     * @param dbName
     * @param login
     * @param password
     * @param dbDriver
     * @return
     * @throws HamException
     */
    DbProxyBuilder addRemoteDbProxy(String connectionString, String login, String password, String dbDriver) throws HamException;

    /**
     * Remove proxy by id
     *
     * @param id
     * @throws HamException
     */
    void removeProxy(String id) throws HamException;

    /**
     * List all current proxies with their states
     *
     * @return
     * @throws HamException
     */
    List<Proxy> retrieveProxies() throws HamException;

    /**
     * List all current db proxies
     *
     * @return
     * @throws HamException
     */
    List<DbProxy> retrieveDbProxies() throws HamException;

    /**
     * Refresh proxy
     *
     * @throws HamException
     */
    void refresh() throws HamException;
}
