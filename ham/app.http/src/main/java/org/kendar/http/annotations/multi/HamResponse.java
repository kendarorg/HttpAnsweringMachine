package org.kendar.http.annotations.multi;

import org.kendar.utils.ConstantsMime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface HamResponse {
    Example[] examples() default {};

    String content() default ConstantsMime.JSON;

    int code() default 200;

    Class<?> body() default Object.class;

    String bodyType() default "";

    boolean array() default false;

    Header[] headers() default {};

    String description() default "";
}
