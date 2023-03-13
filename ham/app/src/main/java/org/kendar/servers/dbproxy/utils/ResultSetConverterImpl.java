package org.kendar.servers.dbproxy.utils;

import org.kendar.janus.JdbcResultSet;
import org.kendar.janus.serialization.JsonTypedSerializer;
import org.kendar.servers.dbproxy.HamResultSet;
import org.kendar.servers.dbproxy.ResultSetConverter;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;

@Component
public class ResultSetConverterImpl implements ResultSetConverter {
    private static JsonTypedSerializer serializer = new JsonTypedSerializer();


    public HamResultSet toHam(ResultSet resultSet) throws Exception {
        if (!(resultSet instanceof JdbcResultSet)) throw new Exception("Invalid resultset");
        var ser = serializer.newInstance();
        ser.write("rs", resultSet);
        String serialized = (String) ser.getSerialized();
        serialized = serialized.replace("org.kendar.janus.JdbcResultSet", "org.kendar.servers.dbproxy.utils.HamResultSetImpl");

        var deser = serializer.newInstance();
        deser.deserialize(serialized);
        return deser.read("rs");
    }

    public ResultSet fromHam(HamResultSet resultSet) {
        var ser = serializer.newInstance();
        ser.write("rs", resultSet);
        String serialized = (String) ser.getSerialized();
        serialized = serialized.replace("org.kendar.servers.dbproxy.utils.HamResultSetImpl", "org.kendar.janus.JdbcResultSet");

        var deser = serializer.newInstance();
        deser.deserialize(serialized);
        return deser.read("rs");
    }
}
