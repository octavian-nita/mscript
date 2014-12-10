package com.webmbt.mscript;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>
 * MScript function signatures can be inferred by scanning {@link com.webmbt.plugin.MScriptInterface.MSCRIPT_METHOD
 * MSCRIPT_METHOD}-annotated Java methods or loaded from {@link java.util.Properties Properties} {@link
 * com.webmbt.mscript.FunctionLibrary#load(java.util.Properties) instances} or {@link
 * com.webmbt.mscript.FunctionLibrary#load(String) files}.
 * </p>
 * <p>
 * In MScript code, a function reference is prefixed by a sigil ($) and the name can optionally be prefixed by a
 * plugin name. MScript functions are <a href="http://en.wikipedia.org/wiki/Variadic_function">variadic</a>.
 * </p>
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

    public boolean isSystemFunction() {
        return pluginName == null || pluginName.trim().length() == 0;
    }

    public String getName() {
        return name;
    }

    /**
     * @return <code>null</code> or empty for 'system' functions
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
     * An MScript function can have multiple implementations in the form of (eventually overloaded) Java methods.
     * Currently, the requirement is these methods to only take {@link java.lang.String} parameters (no varargs) and
     * selecting which actual method is called is done based on the method's arity and the number of arguments provided
     * in the MScript call.
     * </p>
     * <p>
     * The name of the added <code>method</code> does not really matter in identifying the function; this might provide
     * for increased flexibility.
     * </p>
     */
    public Function addImplementation(Method method) {
        if (method == null) {
            throw new NullPointerException("cannot add a null function implementation");
        }

        int parameterCount = method.getParameterCount();
        implementations.put(parameterCount, method); // replaces previously added implementation having the same arity!

        // Update arity, if necessary:
        if (parameterCount < minArity) {
            minArity = parameterCount;
        }
        if (parameterCount > maxArity) {
            maxArity = parameterCount;
        }

        return this;
    }

    @Override
    public String toString() {
        return (pluginName == null ? "$" : "$" + pluginName + ".") + name + "(" + minArity + ".." + maxArity + ")";
    }
}
