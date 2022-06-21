package org.kendar.http.annotations.multi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface HamResponse {
    String content() default "application/json";
    int code() default 200;
    Class<?> body() default Object.class;
    String description() default "";
}
