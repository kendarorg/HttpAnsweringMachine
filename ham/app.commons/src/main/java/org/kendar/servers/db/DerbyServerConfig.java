package org.kendar.servers.db;

import org.kendar.servers.BaseJsonConfig;
import org.kendar.servers.config.ConfigAttribute;

@ConfigAttribute(id="derby")
public class DerbyServerConfig extends BaseJsonConfig<DerbyServerConfig> {
  private boolean active;
  private int port;
  private String user;
  private String password;
  private String path;

  @Override
  public boolean isSystem() {
    return true;
  }

  @Override public DerbyServerConfig copy() {
    return null;
  }

  public String getDerbyDriver(){
    return "org.apache.derby.jdbc.ClientDriver";
  }

  public String getDerbyUrl(){
    return "jdbc:derby://localhost:"+ port;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getUser() {
    return user==null||user.isEmpty()?"root":user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getPassword() {
    return password==null||password.isEmpty()?"root":password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }
}
