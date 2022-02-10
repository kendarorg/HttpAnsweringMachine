package org.kendar.servers;

public abstract class BaseJsonConfig<T> implements Copyable<T> {
  private boolean system;
  private String id;

  public boolean isSystem() {
    return false;
  }

  public void setSystem(boolean system) {

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
