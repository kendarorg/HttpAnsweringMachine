package org.kendar.http.annotations.multi;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PathParameter {
    String key();
    String description() default "";
    String type() default "string";
    String example() default "string";
}
