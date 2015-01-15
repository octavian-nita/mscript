package com.webmbt.mscript;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * MScript functions are <a href="http://en.wikipedia.org/wiki/Variadic_function">variadic</a> and can be built-in
 * (i.e. <em>{@link #isSystemFunction() system}</em> functions) or part of a {@link #getPluginName() named plugin}.
 * </p>
 * <p>
 * By default, a newly created <em>Function</em> has 0 arity (minimum, as well as maximum). Whenever an implementation
 * is {@link #addImplementation(Object, Method) added}, both arities are <em>atomically</em> updated accordingly.
 * </p>
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.2, Jan 12, 2015
 */
public class Function {

    private final String pluginName;

    private final String name;

    private int minArity;

    private int maxArity;

    private final Map<Integer, Implementation> implementations = new HashMap<>();

    public Function(String name) {
        this(name, null);
    }

    public Function(String name, String pluginName) {
        if (name == null || (name = name.trim()).length() == 0) {
            throw new IllegalArgumentException("the name of a function cannot be null or empty");
        }

        this.name = name;
        this.pluginName = pluginName == null || (pluginName = pluginName.trim()).length() == 0 ? null : pluginName;
    }

    /**
     * <em>System</em> (<em>built-in</em>) functions do not belong to any plugin (the {@link #getPluginName()
     * plugin name} is either <code>null</code> or empty) and are invoked in MScript code without any prefix.
     *
     * @return <code>true</code> if <code>this</code> is a <em>system</em> function and <code>false</code> otherwise
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
     * Equivalent to {@link #addImplementation(Object, Method) addImplementation(null, method)}.
     */
    public Function addImplementation(Method method) {
        return addImplementation(null, method);
    }

    /**
     * <p>
     * An MScript function can have multiple implementations in the form of (eventually overloaded) Java {@link Method
     * methods}. Currently, the requirement is these methods to only accept {@link String} parameters (and no varargs).
     * Selecting which actual method gets called and on which target object is done based on the number of arguments
     * provided in the MScript call as well as on the method arity.
     * </p>
     * <p>
     * The name of the added <code>method</code> does not really matter at this point in naming the function; this
     * provides for increased flexibility (i.e. allows different naming / calling conventions in MScript, etc.).
     * </p>
     *
     * @param target instance on which to eventually invoke the <code>method</code>; can be <code>null</code> (if, for
     *               example, <code>method</code> is static or a proper target object is not required or available at
     *               the time of calling this method)
     * @param method the actual Java method that can be invoked upon an MScript function call
     */
    public Function addImplementation(Method method, Object target) {
        if (method == null) {
            throw new NullPointerException("a function (Java) implementation cannot be null");
        }

        // Validate method arguments (currently, only Strings allowed):
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> parametersType : parameterTypes) {
            if (parametersType != String.class) {
                throw new IllegalArgumentException(
                    "function (Java) implementations can currently only take parameters of type java.lang.String");
            }
        }

        // Limit synchronization to the minimum in order to improve lock performance:
        // (see http://plumbr.eu/blog/improving-lock-performance-in-java for details)
        synchronized (implementations) {
            // Eventually replace previously added implementation:
            implementations.put(parameterTypes.length, new Implementation(method, target));

            // Update arity if necessary or if this is the first added implementation:
            if (parameterTypes.length < minArity || implementations.size() == 1) {
                minArity = parameterTypes.length;
            }
            if (parameterTypes.length > maxArity || implementations.size() == 1) {
                maxArity = parameterTypes.length;
            }
        }

        return this;
    }

    @Override
    public String toString() {
        return (pluginName == null ? "$" : "$" + pluginName + ".") + name + "(" + minArity + ".." + maxArity + ")";
    }

    public static class Implementation {

        private final Method method;

        private Object target;

        public Implementation(Method method) {
            this.method = method;
        }

        public Implementation(Method method, Object target) {
            this.target = target;
            this.method = method;
        }

        public Method getMethod() {
            return method;
        }

        public Object getTarget() {
            return target;
        }

        public void setTarget(Object target) {
            this.target = target;
        }
    }
}
