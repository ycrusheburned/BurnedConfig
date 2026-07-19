package com.kizilmc.burnedconfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Optional metadata for a config field. A field does NOT need this annotation to be
 * saved/loaded -- every public, non-transient, non-static field of a @Config class is
 * persisted automatically. Use this only when you want a comment or min/max bounds.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigEntry {
    String comment() default "";
    double min() default Double.NEGATIVE_INFINITY;
    double max() default Double.POSITIVE_INFINITY;
}
