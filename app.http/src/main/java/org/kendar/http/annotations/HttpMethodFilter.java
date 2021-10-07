package org.kendar.http.annotations;

import org.kendar.http.HttpFilterType;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
public @interface HttpMethodFilter {
    /**
     * Sets when the filter will be executed
     * @return
     */
    HttpFilterType phase() default HttpFilterType.NONE;

    /**
     * Wethear it should be blocking or not
     * @return
     */
    boolean blocking() default false;

    /**
     * Can be set using simple ${property:default} or ${property}
     * @return
     */
    String pathAddress() default "";

    /**
     * Can be set using simple ${property:default} or ${property}
     * @return
     */
    String pathPattern() default "";
    String method() default "";
    String name() default "";
    String id();
}
