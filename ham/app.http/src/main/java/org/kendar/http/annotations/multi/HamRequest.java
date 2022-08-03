package org.kendar.http.annotations.multi;

import org.kendar.utils.ConstantsMime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface HamRequest {
    Example[] examples() default {};
    String accept() default ConstantsMime.JSON;
    Class<?> body() default Object.class;
}
