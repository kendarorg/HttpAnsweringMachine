package org.kendar.http.annotations.multi;

public @interface HamSecurity {
    String[] scopes() default {};

    String name();
}
