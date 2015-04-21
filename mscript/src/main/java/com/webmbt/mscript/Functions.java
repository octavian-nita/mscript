package com.webmbt.mscript;

import com.webmbt.plugin.MbtScriptExecutor;
import com.webmbt.plugin.PluginAncestor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.webmbt.mscript.Functions.Lookup.Result;
import static com.webmbt.mscript.Functions.Lookup.Result.E_FUNCTION_NOT_FOUND;
import static com.webmbt.mscript.Functions.Lookup.Result.E_PLUGIN_NOT_FOUND;
import static com.webmbt.mscript.Functions.Lookup.Result.E_WRONG_NUMBER_OF_ARGUMENTS;
import static com.webmbt.mscript.Functions.Lookup.Result.FOUND;
import static com.webmbt.plugin.MScriptInterface.MSCRIPT_METHOD;

/**
 * <p>A (caching) lookup service for MScript {@link Function functions}.</p>
 * <p>
 * {@link #lookup(String, String, int, MbtScriptExecutor, List) Lookup calls} first check an internal function
 * definitions cache. If no suitable definition is found, the provided system functions object and plugins are
 * scanned for {@link MSCRIPT_METHOD}-annotated and public methods (that are cached upon scanning) and if an
 * appropriate definition is found, it is retrieved.
 * </p>
 *
 * @author TestOptimal, LLC
 * @version 1.0, Mar 09, 2015
 */
public class Functions {

    /**
     * A 'global' / default {@link Functions} instance for quick reuse. While function lookup and caching were designed
     * to be thread-safe, one should thoroughly consider before reusing it in a highly concurrent environment (web app,
     * etc.).
     */
    public static final Functions DEFAULT_INSTANCE = new Functions();

    /**
     * In case this basic cache implementation becomes inefficient (memory leaks are detected, the number of available
     * MScript functions grows, etc.) one could consider using {@link java.lang.ref.SoftReference}s or one of the many
     * Java cache implementations around.
     */
    private final ConcurrentMap<String, ConcurrentMap<String, Function>> cache = new ConcurrentHashMap<>();

    /**
     * @param pluginName   use <code>null</code> or an empty string as <code>pluginName</code> in order to get a
     *                     <em>{@link Function#isSystemFunction() system}</em> function
     * @param functionName cannot be <code>null</code> or empty
     * @return the specified {@link Function function} entry from the internal cache or <code>null</code> if no function
     * with the provided name under the provided plugin exists (the plugin and/or the function haven't been cached yet)
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
     * Thread-safe method to eventually create (if non-existent) and retrieve a {@link Function function} entry from the
     * internal cache.
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
            plugin.putIfAbsent(functionName, new Function(functionName, pluginName == "__SYS__" ? null : pluginName));
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
        Result result = E_FUNCTION_NOT_FOUND;
        for (Method method : klass.getMethods()) { // Class#getMethods() returns only public methods!
            Function fn = null;

            if (method.isAnnotationPresent(MSCRIPT_METHOD.class)) {

                // MSCRIPT_METHOD-annotated methods become implementations of MScript functions with the same name:
                fn = getOrCreateFunction(pluginName, method.getName()).addImplementation(method, targetOrClass);

            } else if (method.getDeclaringClass() == klass) {

                // Other public, not inherited methods become implementations with their name prefixed by _:
                fn = getOrCreateFunction(pluginName, "_" + method.getName()).addImplementation(method);

            }

            if (fn != null && fn.getName().equals(functionName) && result != FOUND) {
                // We've just found a function named like the one we were looking for and we don't have a best match yet
                function = fn;
                result = fn.hasImplementation(argsNumber) ? FOUND : E_WRONG_NUMBER_OF_ARGUMENTS;
            }
        }

        return new Lookup(result, function);
    }

    protected Lookup lookupOrCache(String pluginName, String functionName, int argsNumber, Object targetOrClass) {
        // Look up in the internal cache first since reflection-based lookup is generally slower.
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
     *                         the plugins are checked as well in the provided order
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

            // The plugin might exist in the internal cache but it is not available, at least for this lookup:
            return plugin == null ? new Lookup(E_PLUGIN_NOT_FOUND)
                                  : lookupOrCache(pluginName, functionName, argsNumber, plugin);
        }

        // No plugin name - look up system functions and eventually fall back on provided plugins (slower lookup):

        Lookup prevLookup = null;

        if (systemFunctions != null) {
            prevLookup = lookupOrCache(null, functionName, argsNumber, systemFunctions);
            if (prevLookup.result == FOUND) {
                return prevLookup;
            }
        }

        if (availablePlugins != null) {
            for (PluginAncestor availablePlugin : availablePlugins) {
                if (availablePlugin != null) {
                    Lookup currLookup =
                        lookupOrCache(availablePlugin.getPluginID(), functionName, argsNumber, availablePlugin);

                    if (currLookup.result == FOUND) {
                        return currLookup;
                    }

                    if (prevLookup == null || (prevLookup.result == E_FUNCTION_NOT_FOUND &&
                                               currLookup.result == E_WRONG_NUMBER_OF_ARGUMENTS)) {
                        // A better error message, we've found something with the same name at least...
                        prevLookup = currLookup;
                    }
                }
            }
        }

        return prevLookup;
    }

    public void clearCache() { cache.clear(); }

    public static class Lookup {

        public final Result result;

        public final Function function;

        public Lookup(Result result) { this(result, null); }

        public Lookup(Function function) { this(FOUND, function); }

        public Lookup(Result result, Function function) {
            if (result == null) {
                throw new IllegalArgumentException("lookup result cannot be null");
            }
            this.result = result;
            this.function = function;
        }

        public static enum Result {
            FOUND,

            E_PLUGIN_NOT_FOUND, // plugin not found or not accessible
            E_FUNCTION_NOT_FOUND,
            E_WRONG_NUMBER_OF_ARGUMENTS
        }
    }
}
