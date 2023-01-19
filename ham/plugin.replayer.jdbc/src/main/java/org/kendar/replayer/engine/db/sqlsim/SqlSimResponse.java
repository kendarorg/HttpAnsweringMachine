package org.kendar.replayer.engine.db.sqlsim;

public class SqlSimResponse {
    private boolean hasResponse;
    private Object response;

    public boolean isHasResponse() {
        return hasResponse;
    }

    public void setHasResponse(boolean hasResponse) {
        this.hasResponse = hasResponse;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }
}
