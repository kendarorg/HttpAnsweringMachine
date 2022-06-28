package org.kendar.http.annotations.multi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Header {
    String description() default "";
    String key();
    String value() default "";
    Class<?> type() default String.class;
}
