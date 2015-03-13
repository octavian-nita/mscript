package com.webmbt.mscript;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * MScript functions can:
 * <ul>
 * <li>be either built-in (i.e. <em>{@link #isSystemFunction() system}</em> functions) or part of a {@link
 * #getPluginName() named <em>plugin</em>}</li>
 * <li>be <a href="http://en.wikipedia.org/wiki/Function_overloading">overloaded</a></li>
 * </ul>
 * Immediately after creation, an MScript function instance {@link #hasImplementations() has no implementation}.
 * Java-based {@link #addImplementation(Method, Object) implementations} of MScript functions can currently only
 * accept {@link String} parameters and no varargs. The selection of one implementation over another is based on
 * call arity.
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.2, Jan 12, 2015
 */
public class Function {

    private final String name;

    private final String pluginName;

    private final ConcurrentMap<Integer, Implementation> implementations = new ConcurrentHashMap<>();

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

    public String getName() {
        return name;
    }

    /**
     * @return <code>null</code> or empty (whitespace-only) for <em>{@link #isSystemFunction() system}</em> functions
     */
    public String getPluginName() {
        return pluginName;
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
     * @return <code>true</code> if <code>this</code> function has any implementation and <code>false</code> otherwise
     */
    public boolean hasImplementations() {
        return implementations.size() > 0;
    }

    /**
     * @return <code>true</code> if <code>this</code> function has an implementation for the specified
     * <code>arity</code> and <code>false</code> otherwise
     */
    public boolean hasImplementation(int arity) {
        return implementations.containsKey(arity);
    }

    /**
     * Equivalent to {@link #addImplementation(Method, Object) addImplementation(method, null)}.
     */
    public Function addImplementation(Method method) {
        return addImplementation(method, null);
    }

    /**
     * <p>
     * An MScript function can have multiple implementations in the form of (eventually overloaded) Java {@link Method
     * methods}. Currently, the requirement is these methods to only accept {@link String} parameters (and no varargs).
     * Selecting which actual method gets called and on which target object is done based on the number of arguments
     * provided in the MScript call as well as the Java method's arity (i.e. they should be the same).
     * </p>
     * <p>
     * The (Java) name of the added <code>method</code> does not really matter at this point in naming the function;
     * this provides for increased flexibility (i.e. allows different naming / calling conventions in MScript, etc.).
     * </p>
     *
     * @param target instance on which to eventually invoke the <code>method</code>; can be <code>null</code> (if, for
     *               example, <code>method</code> is static or a proper target object is not required or available at
     *               the time of calling this method)
     * @param method the actual Java method that can be invoked upon an MScript function call
     */
    public Function addImplementation(Method method, Object target) {
        if (method == null) {
            throw new IllegalArgumentException("a function (Java) implementation cannot be null");
        }

        // Validate method arguments (currently, only Strings allowed):
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> parametersType : parameterTypes) {
            if (parametersType != String.class) {
                throw new IllegalArgumentException(
                    "function (Java) implementations can currently only take parameters of type java.lang.String");
            }
        }

        // Eventually replace previously added implementation!
        implementations.put(parameterTypes.length, new Implementation(method, target));

        return this;
    }

    @Override
    public String toString() {
        return (pluginName == null ? "$" : "$" + pluginName + ".") + name +
               Arrays.toString(implementations.keySet().toArray());
    }

    public static class Implementation {

        private final Method method;

        private Object target;

        public Implementation(Method method) {
            this.method = method;
        }

        public Implementation(Method method, Object target) {
            this(method);
            this.target = target;
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
