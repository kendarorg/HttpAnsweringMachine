package org.kendar.http.annotations;

import org.kendar.http.annotations.multi.*;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
public  @interface HamDoc {
    String description() default "";
    String produce() default "";
    QueryString[] query() default {};
    PathParameter[] path() default {};
    Header[] header() default {};
    HamRequest[] requests();
    HamResponse[] responses() default {};
}
