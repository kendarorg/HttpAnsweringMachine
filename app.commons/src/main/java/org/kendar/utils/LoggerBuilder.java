package org.kendar.utils;

import org.slf4j.Logger;
import ch.qos.logback.classic.Level;

public interface LoggerBuilder {
    void setLevel(String loggerName, Level level);
    Logger build(Class<?> toLogClass);
}
