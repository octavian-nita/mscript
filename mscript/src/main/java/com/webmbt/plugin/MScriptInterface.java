package com.webmbt.plugin;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author http://testoptimal.com/
 */
public interface MScriptInterface {

    /**
     * Methods annotated with {@link com.webmbt.plugin.MScriptInterface.MSCRIPT_METHOD} represent MScript function
     * implementations and can (currently) accept only {@link java.lang.String} arguments (and no varargs).
     */
    @Target(METHOD)
    @Retention(RUNTIME)
    @interface MSCRIPT_METHOD {}
}
