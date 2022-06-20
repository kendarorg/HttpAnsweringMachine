package org.kendar.http.annotations.multi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Header {
    String key();
    String value();
}
