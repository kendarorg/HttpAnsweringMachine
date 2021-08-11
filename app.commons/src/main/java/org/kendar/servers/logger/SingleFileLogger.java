package org.kendar.servers.logger;

public interface SingleFileLogger {

    void debug(String message,LogPartFlag logPartFlag);
    void debug(String message);
    void error(String message,Exception ex,LogPartFlag logPartFlag);
    void error(String message,Exception ex);
    void error(String message,LogPartFlag logPartFlag);
    void error(String message);
    void info(String message,LogPartFlag logPartFlag);
    void info(String message);
    void trace(String message,LogPartFlag logPartFlag);
    void trace(String message);
    void warn(String message,LogPartFlag logPartFlag);
    void warn(String message);
}
