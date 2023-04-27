package org.kendar.servers.config;


import org.kendar.servers.Copyable;

public class GlobalConfigDb implements Copyable<GlobalConfigDb> {
    private String url;
    private String login;
    private String hibernateDialect;
    private String driver;
    private boolean startInternalH2;
    private String password;

    public String getHibernateDialect() {
        return hibernateDialect;
    }

    public void setHibernateDialect(String hibernateDialect) {
        this.hibernateDialect = hibernateDialect;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public GlobalConfigDb copy() {
        var result = new GlobalConfigDb();
        result.login = login;
        result.url = url;
        result.password = password;
        result.startInternalH2 = startInternalH2;
        result.driver = driver;
        result.hibernateDialect = hibernateDialect;
        return result;
    }

    public boolean isStartInternalH2() {
        return startInternalH2;
    }

    public void setStartInternalH2(boolean startInternalH2) {
        this.startInternalH2 = startInternalH2;
    }
}
