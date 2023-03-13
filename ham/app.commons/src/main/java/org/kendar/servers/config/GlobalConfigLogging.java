package org.kendar.servers.config;

import ch.qos.logback.classic.Level;
import org.kendar.servers.Copyable;

import java.util.HashMap;

public class GlobalConfigLogging implements Copyable<GlobalConfigLogging> {
    private Level logLevel;
    private HashMap<String, Level> loggers = new HashMap<>();


    @Override
    public GlobalConfigLogging copy() {
        var result = new GlobalConfigLogging();
        result.loggers = new HashMap<>(this.loggers);
        result.logLevel = this.logLevel;
        return result;
    }

    public HashMap<String, Level> getLoggers() {
        return loggers;
    }

    public void setLoggers(HashMap<String, Level> loggers) {
        this.loggers = loggers;
    }

    public Level getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }
}
