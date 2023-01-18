package org.kendar.servers.dbproxy;

import java.sql.ResultSet;
import java.util.List;

public interface HamResultSet extends ResultSet {
    void fill(List<List<Object>> sourceData) throws Exception;
}
