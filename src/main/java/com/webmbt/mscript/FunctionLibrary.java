package com.webmbt.mscript;

import com.webmbt.plugin.MScriptInterface;
import com.webmbt.plugin.MbtScriptExecutor;
import com.webmbt.plugin.PluginAncestor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.reflect.Modifier.isPublic;

/**
 * <p>A lookup service for MScript {@link Function function definitions}.</p>
 * <p>
 * {@link #lookup(String, String, int, List) Lookup calls} first check an internal function definitions cache. If no
 * suitable definition is found, the provided system functions object and plugins are scanned for {@link
 * MScriptInterface.MSCRIPT_METHOD}-annotated and public methods (also cached when found) and if an
 * appropriate definition is found, it is retrieved.
 * </p>
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Dec 04, 2014
 */
public class FunctionLibrary {

    //
    // Key to group the <em>{@link Function#isSystemFunction() system}</em> functions symbol (sub)table.
    //
    // !DO NOT HACK AND USE THIS AS THE PLUGIN NAME WHEN CREATING Function INSTANCES!
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

    protected <T> void cache(String pluginName, Class<T> klass) {
        if (klass != null) {
            for (Method method : klass.getMethods()) {
                if (method.isAnnotationPresent(MScriptInterface.MSCRIPT_METHOD.class)) {
                    getOrCreateFunction(pluginName, method.getName()).addImplementation(method);
                } else if (isPublic(method.getModifiers())) {
                    getOrCreateFunction(pluginName, "_" + method.getName()).addImplementation(method);
                }
            }
        }
    }

    protected void cache(MbtScriptExecutor systemFunctions, List<PluginAncestor> plugins) {
        if (systemFunctions != null) {
            cache(null, systemFunctions.getClass());
        }

        if (plugins != null) {
            for (PluginAncestor plugin : plugins) {
                if (plugin != null) {
                    cache(plugin.getPluginID(), plugin.getClass());
                }
            }
        }
    }

    protected LookupResult lookupInCache(String pluginName, String functionName, int argsNumber) {

    }

    /**
     * An MScript parser normally matches separately the plugin name, function name and every passed argument; as such,
     * the signature of this methods is designed so that it is easily callable upon a successful function call match.
     *
     * @param plugins if not <code>null</code> or empty, the function will be looked up only under these plugins; also,
     *                if the plugin name is <code>null</code> or empty and if the function is not found under the added
     *                system functions, these plugins are checked as well, in the provided order
     */
    public LookupResult lookup(String pluginName, String functionName, int argsNumber, List<PluginAncestor> plugins) {
        if (argsNumber < 0) {
            throw new IllegalArgumentException("cannot look up a function with a negative number of arguments");
        }
        if (functionName == null || (functionName = functionName.trim()).length() == 0) {
            throw new IllegalArgumentException("cannot look up a function with a null or empty name");
        }

        if (pluginName != null && (pluginName = pluginName.trim()).length() > 0) {
            if (!pluginNames.contains(pluginName)) {
                return LookupResult.PLUGIN_NOT_ACCESSIBLE;
            }

            ConcurrentMap<String, Function> pluginFunctions = cache.get(pluginName);
            if (pluginFunctions == null) {
                return LookupResult.PLUGIN_NOT_FOUND;
            }

            return checkFunction(pluginFunctions.get(functionName), argsNumber);
        }

        // No plugin name has been provided: check the available system functions and fall back on the provided plugins:
        ConcurrentMap<String, Function> systemFunctions = cache.get(SYSTEM_FUNCTIONS);
        if (systemFunctions == null) {
            return LookupResult.FUNCTION_NOT_FOUND;
        }

        // TODO: check system functions then fall back on the provided plugins
        return LookupResult.OK;

    }

    protected LookupResult checkFunction(Function function, int argsNumber) {
        if (function == null) {
            return LookupResult.FUNCTION_NOT_FOUND;
        }
        if (argsNumber < function.getMinArity()) {
            return LookupResult.TOO_FEW_ARGUMENTS;
        }
        if (argsNumber > function.getMaxArity()) {
            return LookupResult.TOO_MANY_ARGUMENTS;
        }
        return LookupResult.OK;
    }

    public static enum LookupResult {

        OK,

        PLUGIN_NOT_FOUND,
        PLUGIN_NOT_ACCESSIBLE,

        FUNCTION_NOT_FOUND,
        TOO_MANY_ARGUMENTS,
        TOO_FEW_ARGUMENTS;

        /**
         * @return a {@link String} key that can be used to resolve / identify an associated error message
         */
        @Override
        public String toString() {
            // e.g. for an enum constant declared as PLUGIN_NOT_FOUND, we return plugin.not.found
            return super.toString().toLowerCase().replaceAll("_+", ".");
        }
    }
}
