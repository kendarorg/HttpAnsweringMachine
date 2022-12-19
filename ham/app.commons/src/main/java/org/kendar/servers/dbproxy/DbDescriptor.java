package org.kendar.servers.dbproxy;

public class DbDescriptor {
    private String connectionString;
    private String login;
    private String password;

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
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

    public DbDescriptor copy() {
        var result = new DbDescriptor();
        result.setLogin(this.login);
        result.setPassword(this.password);
        result.setConnectionString(this.connectionString);
        return result;
    }
}
