package org.kendar.http.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
public @interface HamMatcher {
    String value();

    MatcherFunction function() default MatcherFunction.STARTS;

    MatcherType type();

    String id() default "";
}
