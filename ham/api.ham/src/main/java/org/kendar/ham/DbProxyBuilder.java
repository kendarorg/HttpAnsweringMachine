package org.kendar.ham;

public interface DbProxyBuilder {
    /**
     * Specify the proxy to expose
     *
     * @param dbName   The name of the db to expose
     * @param login
     * @param password
     * @return the connection string
     */
    String asLocal(String dbName, String login, String password) throws HamException;

    /**
     * Set as inactive (default is active
     *
     * @return
     */
    DbProxyBuilder asInactive();
}
