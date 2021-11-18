package org.kendar.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoggerBuilderImpl implements LoggerBuilder {
    /*public void setLevel(String logger, Level level) {
        Logger root = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.s(Level.INFO);
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();

        LoggerConfig loggerConfig = config.getLoggerConfig(logger);
        LoggerConfig specificConfig = loggerConfig;

        // We need a specific configuration for this logger,
        // otherwise we would change the level of all other loggers
        // having the original configuration as parent as well

        if (!loggerConfig.getName().equals(logger)) {
            specificConfig = new LoggerConfig(logger, level, true);
            specificConfig.setParent(loggerConfig);
            config.addLogger(logger, specificConfig);
        }
        specificConfig.setLevel(level);
        ctx.updateLoggers();
    }

    public void setLevel(Logger logger, Level level) {
        setLevel(logger.getName(),level);
    }*/

    public void setLevel(String loggerName, Level level) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        var logger = loggerContext.getLogger(loggerName);
        logger.setLevel(level);
    }

    public void test(){

    }

    private final ConcurrentHashMap<String,Logger> loggers = new ConcurrentHashMap<>();

    @Override
    public Logger build(Class<?> toLogClass) {
        return loggers.computeIfAbsent(toLogClass.getCanonicalName(),s -> LoggerFactory.getLogger(toLogClass));
    }
}
