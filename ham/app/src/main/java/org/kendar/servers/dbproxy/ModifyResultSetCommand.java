package org.kendar.servers.dbproxy;

import java.util.List;

public class ModifyResultSetCommand {
    private String jsonResultSet;
    private String data;

    public String getJsonResultSet() {
        return jsonResultSet;
    }

    public void setJsonResultSet(String jsonResultSet) {
        this.jsonResultSet = jsonResultSet;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
