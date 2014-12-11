package com.webmbt.mscript;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In MScript code, a function reference is prefixed by a sigil ($) and the name can optionally be prefixed by a
 * plugin name. MScript functions are <a href="http://en.wikipedia.org/wiki/Variadic_function">variadic</a>.
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.1, Dec 04, 2014
 */
public class Function {

    private String pluginName;

    private String name;

    private int minArity;

    private int maxArity;

    private final ConcurrentMap<Integer, Method> implementations = new ConcurrentHashMap<>();

    public Function(String name) {
        this(name, null, 0, 0);
    }

    public Function(String name, String pluginName) {
        this(name, pluginName, 0, 0);
    }

    public Function(String name, int arity) {
        this(name, null, arity, arity);
    }

    public Function(String name, int minArity, int maxArity) {
        this(name, null, minArity, maxArity);
    }

    public Function(String name, String pluginName, int arity) {
        this(name, pluginName, arity, arity);
    }

    public Function(String name, String pluginName, int minArity, int maxArity) {
        if (name == null || (name = name.trim()).length() == 0) {
            throw new IllegalArgumentException("the name of a function cannot be null or empty");
        }
        this.name = name;

        if (pluginName != null) {
            this.pluginName = pluginName.trim();
        }

        if (minArity < 0) {
            throw new IllegalArgumentException("the minimum arity of a function cannot be less than 0");
        }
        this.minArity = minArity;

        if (maxArity < minArity) {
            throw new IllegalArgumentException("the maximum arity of a function cannot be less than its minimum arity");
        }
        this.maxArity = maxArity;
    }

    /**
     * <em>System</em> (or <em>built-in</em>) functions do not belong to any plugin (the {@link #getPluginName()
     * plugin name} is either <code>null</code> or empty) and can be called from MScript without any prefix.
     *
     * @return <code>true</code> if <code>this</code> is a system function and <code>false</code> otherwise
     */
    public boolean isSystemFunction() {
        return pluginName == null || pluginName.trim().length() == 0;
    }

    public String getName() {
        return name;
    }

    /**
     * @return <code>null</code> or empty (whitespace-only) for <em>{@link #isSystemFunction() system}</em> functions
     */
    public String getPluginName() {
        return pluginName;
    }

    public int getMinArity() {
        return minArity;
    }

    public int getMaxArity() {
        return maxArity;
    }

    /**
     * <p>
     * An MScript function can have multiple implementations in the form of (eventually overloaded) Java {@link Method
     * methods}. Currently, the requirement is these methods to only take {@link java.lang.String} parameters (no
     * varargs) and selecting which actual method is called is done based on the method's arity and the number of
     * arguments provided in the MScript call.
     * </p>
     * <p>
     * The name of the added <code>method</code> does not really matter in identifying the function; this provides
     * for increased flexibility (different naming / calling conventions in MScript, etc.).
     * </p>
     */
    public Function addImplementation(Method method) {
        if (method == null) {
            throw new NullPointerException("cannot add a null function (Java) implementation");
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> parametersType : parameterTypes) {
            if (parametersType != String.class) {
                throw new IllegalArgumentException(
                    "function (Java) implementations should only take parameters of type java.lang.String");
            }
        }

        implementations
            .put(parameterTypes.length, method); // replaces previously added implementation having the same arity!

        // Update arity, if necessary:
        if (parameterTypes.length < minArity) {
            minArity = parameterTypes.length;
        }
        if (parameterTypes.length > maxArity) {
            maxArity = parameterTypes.length;
        }

        return this;
    }

    @Override
    public String toString() {
        return (pluginName == null ? "$" : "$" + pluginName + ".") + name + "(" + minArity + ".." + maxArity + ")";
    }
}
