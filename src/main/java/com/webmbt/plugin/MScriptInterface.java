package com.webmbt.plugin;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author http://testoptimal.com/
 */
public interface MScriptInterface {

    @Retention(value = RUNTIME)
    @Target(value = METHOD)
    public static @interface MSCRIPT_METHOD {}
}
