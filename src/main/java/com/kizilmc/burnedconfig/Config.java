package com.kizilmc.burnedconfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a config root. The class must have a public no-arg constructor.
 * File name inside .minecraft/config/ (or the server config dir) will be name() + ".json".
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Config {
    String name();
}
