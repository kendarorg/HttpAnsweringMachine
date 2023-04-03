package org.kendar.mongo.config;

public class MongoDescriptor {
    private String ip;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private int port;
    private String login;
    private String password;


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
        result.setIp(this.ip);
        result.setPort(this.port);
        return result;
    }
}
