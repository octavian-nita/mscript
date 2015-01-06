package com.webmbt.mscript;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * MScript functions are <a href="http://en.wikipedia.org/wiki/Variadic_function">variadic</a> and can be built-in
 * (i.e. <em>{@link #isSystemFunction() system}</em> functions) or part of a {@link #getPluginName() named plugin}.
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.1, Dec 04, 2014
 */
public class Function {

    private String pluginName;

    private String name;

    private int minArity;

    private int maxArity;

    // Manipulating function implementations can be done in a concurrent environment (web app, etc.)!
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
     * plugin name} is either <code>null</code> or empty) and are invoked in MScript code without any prefix.
     *
     * @return <code>true</code> if <code>this</code> is a system function and <code>false</code> otherwise
     */
    public boolean isSystemFunction() {
        return pluginName == null || pluginName.trim().length() == 0;
    }

    /**
     * @return <code>null</code> or empty (whitespace-only) for <em>{@link #isSystemFunction() system}</em> functions
     */
    public String getPluginName() {
        return pluginName;
    }

    public String getName() {
        return name;
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
     * methods}. Currently, the requirement is these methods to only accept {@link String} parameters (and no varargs).
     * Selecting which actual Java method gets called is done based on the number of arguments provided in the MScript
     * call as well as on the method arity.
     * </p>
     * <p>
     * The name of the added <code>method</code> does not really matter in identifying the function; this provides
     * for increased flexibility (i.e. allows different naming / calling conventions in MScript, etc.).
     * </p>
     */
    public Function addImplementation(Method method) {
        if (method == null) {
            throw new NullPointerException("cannot add a null function (Java) implementation");
        }

        // Validate method arguments (currently, only Strings allowed):
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> parametersType : parameterTypes) {
            if (parametersType != String.class) {
                throw new IllegalArgumentException(
                    "function (Java) implementations should only take parameters of type java.lang.String");
            }
        }

        implementations  // eventually replace previously added implementation with the same arity!
            .put(parameterTypes.length, method);

        // Update arity, if necessary or if this is the first added implementation:
        if (parameterTypes.length < minArity || implementations.size() == 1) {
            minArity = parameterTypes.length;
        }
        if (parameterTypes.length > maxArity || implementations.size() == 1) {
            maxArity = parameterTypes.length;
        }

        return this;
    }

    @Override
    public String toString() {
        return (pluginName == null ? "$" : "$" + pluginName + ".") + name + "(" + minArity + ".." + maxArity + ")";
    }
}
