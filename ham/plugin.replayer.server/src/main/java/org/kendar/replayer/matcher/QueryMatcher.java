package org.kendar.replayer.matcher;

import org.kendar.servers.http.Request;
import org.kendar.servers.http.matchers.FilterMatcher;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.function.Function;

@Component
public class QueryMatcher implements FilterMatcher {
    private String dbName;
    @Override
    public boolean matches(Request req) {
        if(dbName==null) return false;
        if(!req.getPath().toLowerCase(Locale.ROOT).startsWith(
                ("/api/db/"+dbName).toLowerCase(Locale.ROOT))){
            return false;
        }
        return false;
    }

    @Override
    public void initialize(Function<String, String> apply) {

    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
    @Override
    public boolean validate() {
        return isValid(dbName);
    }

    private boolean isValid(String val) {
        return val!=null&&val.length()>0;
    }
}
