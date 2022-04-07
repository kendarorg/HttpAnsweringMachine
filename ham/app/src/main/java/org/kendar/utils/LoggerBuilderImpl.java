package org.kendar.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoggerBuilderImpl implements LoggerBuilder {

    public void setLevel(String loggerName, Level level) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        var logger = loggerContext.getLogger(loggerName);
        logger.setLevel(level);
    }

    @Override
    public Level getLevel(String loggerName) {
        if(!loggers.contains(loggerName)) return Level.OFF;
        var logger = loggers.get(loggerName);
        if(logger.isDebugEnabled()&& logger.isTraceEnabled() && logger.isErrorEnabled() &&
                logger.isInfoEnabled() && logger.isWarnEnabled()) return Level.ALL;
        if(logger.isDebugEnabled())return Level.DEBUG;
        if(logger.isWarnEnabled()) return Level.WARN;
        if(logger.isInfoEnabled()) return Level.INFO;
        if(logger.isTraceEnabled()) return Level.TRACE;
        if(logger.isErrorEnabled()) return Level.ERROR;
        return Level.OFF;
    }

    public void test(){

    }

    private final ConcurrentHashMap<String,Logger> loggers = new ConcurrentHashMap<>();

    @Override
    public Logger build(Class<?> toLogClass) {
        return loggers.computeIfAbsent(toLogClass.getCanonicalName(),s -> LoggerFactory.getLogger(toLogClass));
    }
}
