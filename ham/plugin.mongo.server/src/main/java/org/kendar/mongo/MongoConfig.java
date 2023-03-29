package org.kendar.mongo;

import org.kendar.servers.BaseJsonConfig;
import org.kendar.servers.SpecialJsonConfig;
import org.kendar.servers.config.ConfigAttribute;


@ConfigAttribute(id = "mongo")
public class MongoConfig extends BaseJsonConfig<MongoConfig> implements SpecialJsonConfig {
    private boolean active;
    private int port;

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

    @Override
    public MongoConfig copy() {
        var result = new MongoConfig();
        result.active = this.active;
        result.port = this.port;
        result.setId(this.getId());
        return result;
    }

    @Override
    public void preSave() {

    }
}
