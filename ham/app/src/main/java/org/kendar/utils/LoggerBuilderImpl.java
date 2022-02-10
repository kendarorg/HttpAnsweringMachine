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

    public void test(){

    }

    private final ConcurrentHashMap<String,Logger> loggers = new ConcurrentHashMap<>();

    @Override
    public Logger build(Class<?> toLogClass) {
        return loggers.computeIfAbsent(toLogClass.getCanonicalName(),s -> LoggerFactory.getLogger(toLogClass));
    }
}
