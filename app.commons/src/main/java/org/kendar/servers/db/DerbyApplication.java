package org.kendar.servers.db;

import java.sql.Connection;

public interface DerbyApplication {
    String dbName();
    String canaryTable();
    void initializeDb(Connection conn);
    void resetDb();
    String connectionString();
}
