package org.kendar.replayer;

import org.kendar.servers.BaseJsonConfig;
import org.kendar.servers.config.ConfigAttribute;

@ConfigAttribute(id = "replayer.server")
public class ReplayerConfig extends BaseJsonConfig<ReplayerConfig> {

    @Override
    public ReplayerConfig copy() {
        var result = new ReplayerConfig();
        return result;
    }
}
