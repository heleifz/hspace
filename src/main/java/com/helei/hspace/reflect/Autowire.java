package com.helei.hspace.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Autowire {
    /**
     * default autowire is by name
     */
    boolean byType() default false;
    /**
     * dont use autowire on this field
     */
    boolean ignore() default false;
};
