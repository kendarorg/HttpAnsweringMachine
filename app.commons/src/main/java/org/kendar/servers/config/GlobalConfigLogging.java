package org.kendar.servers.config;

public class GlobalConfigLogging {
    private MultilevelLoggingConfig request;
    private MultilevelLoggingConfig response;
    private boolean path;
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

    public boolean isPath() {
        return path;
    }

    public void setPath(boolean path) {
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
}
