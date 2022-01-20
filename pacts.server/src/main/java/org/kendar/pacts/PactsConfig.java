package org.kendar.pacts;

import org.kendar.servers.BaseJsonConfig;
import org.kendar.servers.config.ConfigAttribute;

@ConfigAttribute(id="pacts.server")
public class PactsConfig extends BaseJsonConfig<PactsConfig> {
    private String path;

    @Override public PactsConfig copy() {
        var result = new PactsConfig();
        result.path = this.path;
        return result;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
