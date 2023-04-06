package org.kendar.mongo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class LocalLoggerBuilderImpl implements LoggerBuilder {
    private final ConcurrentHashMap<String, Logger> loggers = new ConcurrentHashMap<>();

    @Override
    public void setLevel(String loggerName, Level level) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        var logger = loggerContext.getLogger(loggerName);
        logger.setLevel(level);
    }

    @Override
    public Level getLevel(String loggerName) {
        if (!loggers.containsKey(loggerName)) return Level.OFF;
        var logger = loggers.get(loggerName);
        if (logger.isDebugEnabled() && logger.isTraceEnabled() && logger.isErrorEnabled() &&
                logger.isInfoEnabled() && logger.isWarnEnabled()) return Level.ALL;
        if (logger.isDebugEnabled()) return Level.DEBUG;
        if (logger.isWarnEnabled()) return Level.WARN;
        if (logger.isInfoEnabled()) return Level.INFO;
        if (logger.isTraceEnabled()) return Level.TRACE;
        if (logger.isErrorEnabled()) return Level.ERROR;
        return Level.OFF;
    }

    @Override
    public Logger build(Class<?> toLogClass) {
        return loggers.computeIfAbsent(toLogClass.getCanonicalName(), s -> LoggerFactory.getLogger(toLogClass));
    }
}