package org.kendar.servers.http;

import org.kendar.servers.BaseJsonConfig;
import org.kendar.servers.config.ConfigAttribute;

@ConfigAttribute(id = "js.filters")
public class JsFilterConfig extends BaseJsonConfig<JsFilterConfig> {

    @Override
    public JsFilterConfig copy() {
        var result = new JsFilterConfig();
        result.setId(this.getId());
        return result;
    }
}
