package com.webmbt.mscript;

import com.webmbt.plugin.MbtScriptExecutor;
import com.webmbt.plugin.PluginAncestor;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import static com.webmbt.mscript.FunctionLibrary.Lookup.State.FUNCTION_NOT_FOUND;
import static com.webmbt.mscript.FunctionLibrary.Lookup.State.OK;
import static com.webmbt.mscript.FunctionLibrary.Lookup.State.PLUGIN_NOT_ACCESSIBLE;
import static com.webmbt.mscript.FunctionLibrary.Lookup.State.PLUGIN_NOT_FOUND;
import static com.webmbt.mscript.FunctionLibrary.Lookup.State.TOO_FEW_ARGUMENTS;
import static com.webmbt.mscript.FunctionLibrary.Lookup.State.TOO_MANY_ARGUMENTS;
import static com.webmbt.plugin.MScriptInterface.MSCRIPT_METHOD;
import static java.lang.reflect.Modifier.isPublic;

/**
 * <p>A lookup service for MScript {@link Function function definitions}.</p>
 * <p>
 * {@link #lookup(String, String, int, MbtScriptExecutor, List) Lookup calls} first check an internal function
 * definitions cache. If no suitable definition is found the provided system functions object and plugins are scanned
 * for {@link MSCRIPT_METHOD}-annotated and public methods (cached upon scanning) and if an appropriate definition is
 * found, it is retrieved.
 * </p>
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Dec 04, 2014
 */
public class FunctionLibrary {

    private static final Logger log = Logger.getLogger(FunctionLibrary.class.getName());

    //
    // Key to group the <em>{@link Function#isSystemFunction() system}</em> functions symbol (sub)table.
    //
    private static final String SYSTEM_FUNCTIONS = "__SYS__";

    private final ConcurrentMap<String, ConcurrentMap<String, Function>> cache = new ConcurrentHashMap<>();

    /**
     * Thread-safe method to eventually create (if non-existent) and retrieve a <em>plugin</em>.
     *
     * @param pluginName if <code>null</code> or empty (whitespace-only), the <em>{@link Function#isSystemFunction()
     *                   system}</em> functions are returned
     */
    protected final ConcurrentMap<String, Function> getOrCreatePlugin(String pluginName) {
        if (pluginName == null || (pluginName = pluginName.trim()).length() == 0) {
            pluginName = SYSTEM_FUNCTIONS;
        }

        // See
        // http://stackoverflow.com/questions/10743622/concurrenthashmap-avoid-extra-object-creation-with-putifabsent
        // for an explanation as to why the following is acceptable from a thread-safety point of view:
        ConcurrentMap<String, Function> plugin = cache.get(pluginName);
        if (plugin == null) {
            cache.putIfAbsent(pluginName, new ConcurrentHashMap<String, Function>());
            plugin = cache.get(pluginName);
        }

        return plugin;
    }

    /**
     * Thread-safe method to eventually create (if non-existent) and retrieve a {@link Function function}. If the
     * function is created, no arity is explicitly provided.
     *
     * @param pluginName if <code>null</code> or empty (whitespace-only), the function is considered to be a
     *                   <em>{@link Function#isSystemFunction() system}</em> function
     */
    protected final Function getOrCreateFunction(String pluginName, String functionName) {
        if (functionName == null || (functionName = functionName.trim()).length() == 0) {
            throw new IllegalArgumentException("the name of a function cannot be null or empty");
        }

        ConcurrentMap<String, Function> plugin = getOrCreatePlugin(pluginName);

        // See
        // http://stackoverflow.com/questions/10743622/concurrenthashmap-avoid-extra-object-creation-with-putifabsent
        // for an explanation as to why the following is acceptable from a thread-safety point of view:
        Function function = plugin.get(functionName);
        if (function == null) {
            plugin.putIfAbsent(functionName, new Function(functionName, pluginName));
            function = plugin.get(functionName);
        }

        return function;
    }

    protected void cache(String pluginName, Object target) {
        if (target == null) {
            log.warning("cannot cache function implementations on a null target object; skipping cache request...");
            return;
        }

        for (Method method : target.getClass().getMethods()) {
            if (method.isAnnotationPresent(MSCRIPT_METHOD.class)) {

                // MSCRIPT_METHOD-annotated methods become implementations of MScript functions with the same name:
                getOrCreateFunction(pluginName, method.getName()).addImplementation(method, target);

            } else if (isPublic(method.getModifiers())) {

                // Other public methods become implementations of MScript functions with the name prefixed by '_':
                getOrCreateFunction(pluginName, "_" + method.getName()).addImplementation(method);

            }
        }
    }

    protected void cache(MbtScriptExecutor systemFunctions, List<PluginAncestor> plugins) {
        if (systemFunctions != null) {
            cache(null, systemFunctions);
        }

        if (plugins != null) {
            for (PluginAncestor plugin : plugins) {
                if (plugin != null) {
                    cache(plugin.getPluginID(), plugin);
                }
            }
        }
    }

    protected Lookup lookup(String pluginName, String functionName, int argsNumber,
                            Collection<String> accessiblePluginNames) {
        if (argsNumber < 0) {
            throw new IllegalArgumentException("cannot look up a function with a negative number of arguments");
        }
        if (functionName == null || (functionName = functionName.trim()).length() == 0) {
            throw new IllegalArgumentException("cannot look up a function with a null or empty name");
        }

        if (pluginName == null || (pluginName = pluginName.trim()).length() == 0) {
            return lookupInCache(null, functionName, argsNumber);
        }

        if (accessiblePluginNames == null || accessiblePluginNames.size() == 0) {
            log.fine("no plugin names to look up in cache provided; looking up in the entire cache...");
        } else if (!accessiblePluginNames.contains(pluginName)) {
            return new Lookup(PLUGIN_NOT_ACCESSIBLE);
        }

        return lookupInCache(pluginName, functionName, argsNumber);
    }

    private Lookup lookupInCache(String pluginName, String functionName, int argsNumber) {
        if (pluginName == null || (pluginName = pluginName.trim()).length() == 0) {
            pluginName = SYSTEM_FUNCTIONS;
        }

        ConcurrentMap<String, Function> functions = cache.get(pluginName);
        if (functions == null) {
            return SYSTEM_FUNCTIONS.equals(pluginName) ? new Lookup(FUNCTION_NOT_FOUND) : new Lookup(PLUGIN_NOT_FOUND);
        }

        Function function = functions.get(functionName);
        if (function == null) {
            return new Lookup(FUNCTION_NOT_FOUND);
        }

        if (argsNumber < function.getMinArity()) {
            return new Lookup(TOO_FEW_ARGUMENTS);
        }

        if (argsNumber > function.getMaxArity()) {
            return new Lookup(TOO_MANY_ARGUMENTS);
        }

        return new Lookup(function);
    }

    /**
     * An MScript parser normally matches separately the plugin name, function name and every passed argument; as such,
     * the signature of this methods is designed so that it is easily callable upon a successful function call match.
     *
     * @param accessiblePlugins if not <code>null</code> or empty, the function will be looked up only under these
     *                          plugins; also if the plugin name is <code>null</code> or empty and if the function
     *                          is not found under the added system functions, these plugins are checked as well,
     *                          in the provided order
     */
    public Lookup lookup(String pluginName, String functionName, int argsNumber, MbtScriptExecutor systemFunctions,
                         List<PluginAncestor> accessiblePlugins) {

        Lookup lookup = lookupInCache(pluginName, functionName, argsNumber);
        if (lookup.state == OK) {
            return lookup;
        }

        if (pluginName == null || (pluginName = pluginName.trim()).length() == 0 ||
            SYSTEM_FUNCTIONS.equals(pluginName)) {
            // No plugin name provided, look up in system functions and then the provided plugins, one by one:
        }

        // A plugin name was provided, look up in matching plugin:

//        Lookup lookup = lookup(pluginName, functionName, argsNumber, accessiblePluginNames);
//        switch (lookup.state) {
//
//        case PLUGIN_NOT_FOUND:
//        case FUNCTION_NOT_FOUND:
//            cache(systemFunctions, accessiblePlugins);
//            lookup = lookup(pluginName, functionName, argsNumber, accessiblePluginNames);
//
//            if ((pluginName == null || (pluginName = pluginName.trim()).length() == 0) &&
//                (lookup.state == PLUGIN_NOT_FOUND || FUNCTION_NOT_FOUND)) {
//
//            }
//
//        default:
//            return lookup;
//        }
    }

    public static class Lookup {

        public final Function function;

        public final State state;

        public Lookup(Function function) {
            this(OK, function);
        }

        public Lookup(State state) {
            this(state, null);
        }

        public Lookup(State state, Function function) {
            if (state == null) {
                throw new NullPointerException("lookup result state cannot be null");
            }
            this.function = function;
            this.state = state;
        }

        public static enum State {

            OK,

            PLUGIN_NOT_ACCESSIBLE,
            PLUGIN_NOT_FOUND,

            FUNCTION_NOT_FOUND,
            TOO_MANY_ARGUMENTS,
            TOO_FEW_ARGUMENTS;

            /**
             * @return a {@link String} key that can be used to resolve / identify an associated error message
             */
            @Override
            public String toString() {
                // e.g. for an enum constant declared as PLUGIN_NOT_FOUND, return plugin.not.found
                return super.toString().toLowerCase().replaceAll("_+", ".");
            }
        }
    }
}
