package org.kendar.servers.config;

import org.kendar.servers.Copyable;

public class GlobalConfigLogging implements Copyable<GlobalConfigLogging> {
    private MultilevelLoggingConfig request;
    private MultilevelLoggingConfig response;
    private String path;
    private boolean statics;
    private boolean dynamic;

    public MultilevelLoggingConfig getRequest() {
        return request;
    }

    public void setRequest(MultilevelLoggingConfig request) {
        this.request = request;
    }

    public MultilevelLoggingConfig getResponse() {
        return response;
    }

    public void setResponse(MultilevelLoggingConfig response) {
        this.response = response;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isStatics() {
        return statics;
    }

    public void setStatics(boolean statics) {
        this.statics = statics;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    @Override public GlobalConfigLogging copy() {
        var result = new GlobalConfigLogging();
        result.dynamic = this.dynamic;
        result.path = this.path;
        result.request = this.request.copy();
        result.response = this.response.copy();
        result.statics = this.statics;
        return result;
    }
}
