package org.kendar.mongo.config;

public class MongoDescriptor {
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

    public MongoDescriptor copy() {
        var result = new MongoDescriptor();
        result.setLogin(this.login);
        result.setPassword(this.password);
        result.setConnectionString(this.connectionString);
        return result;
    }
}
