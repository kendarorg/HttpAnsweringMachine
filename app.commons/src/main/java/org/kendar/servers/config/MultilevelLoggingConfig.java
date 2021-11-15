package org.kendar.servers.config;

import org.kendar.servers.Copyable;

public class MultilevelLoggingConfig implements Copyable<MultilevelLoggingConfig> {
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

    @Override public MultilevelLoggingConfig copy() {
        var result = new MultilevelLoggingConfig();
        result.basic = this.basic;
        result.full = this.full;
        return result;
    }
}
