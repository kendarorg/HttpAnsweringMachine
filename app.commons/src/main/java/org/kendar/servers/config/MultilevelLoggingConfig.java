package org.kendar.servers.config;

public class MultilevelLoggingConfig {
    private boolean basic;
    private boolean full;

    public boolean isBasic() {
        return basic;
    }

    public void setBasic(boolean basic) {
        this.basic = basic;
    }

    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        this.full = full;
    }
}
