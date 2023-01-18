package org.kendar.replayer.engine.db;

import org.apache.commons.lang3.ClassUtils;
import org.kendar.janus.cmd.interfaces.JdbcCommand;
import org.kendar.janus.cmd.interfaces.JdbcSqlCommand;
import org.kendar.janus.serialization.JsonTypedSerializer;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.matchers.FilterMatcher;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.function.Function;

@Component
public class QueryMatcher implements FilterMatcher {

    private static JsonTypedSerializer serializer = new JsonTypedSerializer();
    private String dbName;
    private String sql;
    @Override
    public boolean matches(Request req) {
        if(dbName==null) return false;
        if(!req.getPath().toLowerCase(Locale.ROOT).startsWith(
                ("/api/db/"+dbName).toLowerCase(Locale.ROOT))){
            return false;
        }
        var reqDeser = serializer.newInstance();
        JdbcCommand cmd;
        try {
            reqDeser.deserialize(req.getRequestText());
            cmd = reqDeser.read("command");
            if(cmd==null) return false;
        }catch (Exception ex){
            return false;
        }
        if(ClassUtils.isAssignable(cmd.getClass(), JdbcSqlCommand.class)){
            var sql = ((JdbcSqlCommand)cmd).getSql();
            if(!sql.trim().equalsIgnoreCase(sql.trim())){
                return false;
            }

            return true;
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

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
