package org.kendar.utils;

import org.slf4j.Logger;

public interface LoggerBuilder {
    Logger build(Class<?> toLogClass);
}
