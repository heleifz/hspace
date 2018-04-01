package com.helei.hspace.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * resource for singleton ioc container resource
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Resource {
    /**
     * if id is emtpy, resource id is full class name
     */
    String id() default "";
    /**
     * if auto wire is true, then all its properties that dont have initializer 
     * is autowired.
     */
    boolean autowireProperty() default false;
}