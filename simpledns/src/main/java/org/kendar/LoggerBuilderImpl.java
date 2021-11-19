package org.kendar;

import ch.qos.logback.classic.Level;
import org.kendar.utils.LoggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoggerBuilderImpl implements LoggerBuilder {
    private final ConcurrentHashMap<String,Logger> loggers = new ConcurrentHashMap<>();

    @Override public void setLevel(String loggerName, Level level) {

    }

    @Override
    public Logger build(Class<?> toLogClass) {
        return loggers.computeIfAbsent(toLogClass.getCanonicalName(),s -> LoggerFactory.getLogger(toLogClass));
    }
}
