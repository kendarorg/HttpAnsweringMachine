package org.kendar.http.annotations.multi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface QueryString {
    String key();

    String description() default "";

    String type() default "string";

    String example() default "string";
}
