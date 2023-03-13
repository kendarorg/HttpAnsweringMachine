package org.kendar.http.annotations.multi;

public @interface Example {
    String exampleFunction() default "";

    String example() default "";

    String description() default "";
}
