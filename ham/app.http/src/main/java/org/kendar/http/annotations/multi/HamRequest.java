package org.kendar.http.annotations.multi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface HamRequest {
    Example[] examples() default {};
    String accept() default "application/json";
    Class<?> body() default Object.class;
}
