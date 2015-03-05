package com.webmbt.mscript;

import com.webmbt.plugin.MbtScriptExecutor;
import com.webmbt.plugin.PluginAncestor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import static com.webmbt.mscript.Functions.Lookup.Result;
import static com.webmbt.mscript.Functions.Lookup.Result.FOUND;
import static com.webmbt.mscript.Functions.Lookup.Result.FUNCTION_NOT_FOUND;
import static com.webmbt.mscript.Functions.Lookup.Result.PLUGIN_NOT_FOUND;
import static com.webmbt.mscript.Functions.Lookup.Result.WRONG_NUMBER_OF_ARGUMENTS;
import static com.webmbt.plugin.MScriptInterface.MSCRIPT_METHOD;

/**
 * <p>A (caching) lookup service for MScript {@link Function functions}.</p>
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
public class Functions {

    protected final static Logger log = Logger.getLogger(Functions.class.getName());

    private final ConcurrentMap<String, ConcurrentMap<String, Function>> cache = new ConcurrentHashMap<>();

    /**
     * Use <code>null</code> or empty string as <code>pluginName</code> in order to get a <em>{@link
     * Function#isSystemFunction() system}</em> function.
     */
    protected final Function getFunction(String pluginName, String functionName) {
        if (pluginName == null || (pluginName = pluginName.trim()).length() == 0) {
            pluginName = "__SYS__";
        }

        if (functionName == null || (functionName = functionName.trim()).length() == 0) {
            throw new IllegalArgumentException("the name of a function cannot be null or empty");
        }

        ConcurrentMap<String, Function> plugin = cache.get(pluginName);
        return plugin == null ? null : plugin.get(functionName);
    }

    /**
     * Thread-safe method to eventually create (if non-existent) and retrieve a {@link Function function}.
     *
     * @param pluginName if <code>null</code> or empty, the function is considered to be a <em>{@link
     *                   Function#isSystemFunction() system}</em> function
     */
    protected final Function getOrCreateFunction(String pluginName, String functionName) {
        if (pluginName == null || (pluginName = pluginName.trim()).length() == 0) {
            pluginName = "__SYS__";
        }

        if (functionName == null || (functionName = functionName.trim()).length() == 0) {
            throw new IllegalArgumentException("the name of a function cannot be null or empty");
        }

        // See @Bohemian's (accepted) answer to the question at
        // http://stackoverflow.com/questions/10743622/concurrenthashmap-avoid-extra-object-creation-with-putifabsent
        // for an explanation as to why the following is acceptable from a thread-safety point of view:

        ConcurrentMap<String, Function> plugin = cache.get(pluginName);
        if (plugin == null) {
            cache.putIfAbsent(pluginName, new ConcurrentHashMap<String, Function>());
            plugin = cache.get(pluginName);
        }

        Function function = plugin.get(functionName);
        if (function == null) {
            plugin.putIfAbsent(functionName, new Function(functionName, pluginName));
            function = plugin.get(functionName);
        }

        return function;
    }

    protected Lookup lookupAndCache(String pluginName, String functionName, int argsNumber, Object targetOrClass) {
        if (targetOrClass == null) {
            throw new IllegalArgumentException("cannot cache function implementations on a null target");
        }
        Class<?> klass = targetOrClass instanceof Class ? (Class<?>) targetOrClass : targetOrClass.getClass();

        Function function = null;
        Result result = FUNCTION_NOT_FOUND;
        for (Method method : klass.getClass().getMethods()) {
            Function fn = null;

            if (method.isAnnotationPresent(MSCRIPT_METHOD.class)) {

                // MSCRIPT_METHOD-annotated methods become implementations of MScript functions with the same name:
                fn = getOrCreateFunction(pluginName, method.getName()).addImplementation(method, targetOrClass);

            } else if (method.getDeclaringClass() != Object.class) { // Class#getMethods() returns only public methods!

                // Other public methods not inherited from Object become implementations with their name prefixed by _:
                fn = getOrCreateFunction(pluginName, "_" + method.getName()).addImplementation(method);

            }

            if (fn != null && fn.getName().equals(functionName) && result != FOUND) {
                // We've just found a function named like the one we were looking for and we don't have a best match yet
                function = fn;
                result = fn.hasImplementation(argsNumber) ? FOUND : WRONG_NUMBER_OF_ARGUMENTS;
            }
        }

        return new Lookup(result, function);
    }

    protected Lookup lookup(String pluginName, String functionName, int argsNumber, Object targetOrClass) {
        // Look up in the cache first since reflection-based lookup is generally slower.
        Function function = getFunction(pluginName, functionName);
        if (function != null && function.hasImplementation(argsNumber)) {
            return new Lookup(function);
        }

        // NOTE: even if the plugin has already been cached, if we can't find the requested function (either at
        // all or having a corresponding implementation for the provided arity, we re-cache the whole plugin in
        // case the plugin list or object reference has dynamically changed over the course of the program.
        return lookupAndCache(pluginName, functionName, argsNumber, targetOrClass);
    }

    /**
     * An MScript parser normally matches separately the plugin name, function name and every passed argument; as such,
     * the signature of this methods is designed so that it is easily callable upon a successful function call match.
     *
     * @param availablePlugins the function will be looked up only under these plugins; also, if the plugin name is
     *                         <code>null</code> or empty and if the function is not found under the system functions,
     *                         these plugins are checked as well in the provided order
     */
    public Lookup lookup(String pluginName, String functionName, int argsNumber, MbtScriptExecutor systemFunctions,
                         List<PluginAncestor> availablePlugins) {

        if (pluginName != null && (pluginName = pluginName.trim()).length() > 0) {
            // Probably a faster lookup...

            // First, make sure we have access to the requested plugin (i.e. we find it in the provided plugin list) and
            // since we're scanning the plugins anyway, try to identify the one we will lookup into if we don't find the
            // function in the cache.
            PluginAncestor plugin = null;
            if (availablePlugins != null) {
                for (PluginAncestor availablePlugin : availablePlugins) {
                    if (availablePlugin != null && pluginName.equals(availablePlugin.getPluginID())) {
                        plugin = availablePlugin;
                        break;
                    }
                }
            }

            // The plugin might exist but it is not available, at least for this lookup:
            return plugin == null ? new Lookup(PLUGIN_NOT_FOUND) : lookup(pluginName, functionName, argsNumber, plugin);
        }

        // No plugin name - look up system functions and fall back on provided plugins (slower lookup):

        Lookup prevLookup = lookup(null, functionName, argsNumber, systemFunctions);
        if (prevLookup.result == FOUND) {
            return prevLookup;
        }

        if (availablePlugins != null) {
            for (PluginAncestor availablePlugin : availablePlugins) {
                if (availablePlugin != null) {
                    Lookup currLookup =
                        lookup(availablePlugin.getPluginID(), functionName, argsNumber, availablePlugin);

                    if (currLookup.result == FOUND) {
                        return currLookup;
                    }

                    if (prevLookup.result == FUNCTION_NOT_FOUND && currLookup.result == WRONG_NUMBER_OF_ARGUMENTS) {
                        // A better error message, we've found something similar at least...
                        prevLookup = currLookup;
                    }
                }
            }
        }

        return prevLookup;
    }

    public static class Lookup {

        public final Function function;

        public final Result result;

        public Lookup(Function function) {
            this(FOUND, function);
        }

        public Lookup(Result result) {
            this(result, null);
        }

        public Lookup(Result result, Function function) {
            if (result == null) {
                throw new IllegalArgumentException("lookup result cannot be null");
            }
            this.function = function;
            this.result = result;
        }

        public static enum Result {

            FOUND,

            PLUGIN_NOT_FOUND, // plugin not found or not accessible

            FUNCTION_NOT_FOUND,
            WRONG_NUMBER_OF_ARGUMENTS;

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
