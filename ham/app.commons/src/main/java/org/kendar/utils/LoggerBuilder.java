package org.kendar.utils;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;

public interface LoggerBuilder {
    void setLevel(String loggerName, Level level);

    Level getLevel(String loggerName);

    Logger build(Class<?> toLogClass);
}
