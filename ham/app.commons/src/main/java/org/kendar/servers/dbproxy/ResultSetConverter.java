package org.kendar.servers.dbproxy;

import java.sql.ResultSet;

public interface ResultSetConverter {
    HamResultSet toHam(ResultSet resultSet) throws Exception;

    ResultSet fromHam(HamResultSet resultSet);
}
