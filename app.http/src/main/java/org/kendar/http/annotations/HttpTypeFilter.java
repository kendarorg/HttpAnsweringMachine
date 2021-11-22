package org.kendar.http.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface HttpTypeFilter {
    /**
     * Can be set using simple ${property:default} or ${property}
     * @return
     */
    String hostAddress() default "";

    /**
     * Can be set using simple ${property:default} or ${property}
     * @return
     */
    String hostPattern() default "";
    String name() default "";
    int priority() default 100;

    /**
     * Propagates on children
     * @return
     */
    boolean blocking() default false;
}
