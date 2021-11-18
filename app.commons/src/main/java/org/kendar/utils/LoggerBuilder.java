package org.kendar.utils;

import org.slf4j.Logger;

public interface LoggerBuilder {
    //void setLevel(String logger, Level level);
    Logger build(Class<?> toLogClass);
}
