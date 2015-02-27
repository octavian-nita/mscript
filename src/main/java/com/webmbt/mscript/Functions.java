package com.webmbt.mscript;

import com.webmbt.plugin.MbtScriptExecutor;
import com.webmbt.plugin.PluginAncestor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import static com.webmbt.mscript.Functions.Lookup.Result.FOUND;
import static com.webmbt.mscript.Functions.Lookup.Result.FUNCTION_NOT_FOUND;
import static com.webmbt.mscript.Functions.Lookup.Result.PLUGIN_NOT_FOUND;
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

    private static final Logger log = Logger.getLogger(Functions.class.getName());

    //
    // Key to identify the <em>{@link Function#isSystemFunction() system}</em> functions cache.
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

            } else if (method.getDeclaringClass() != Object.class) { // Class#getMethods() only returns public methods!

                // Other public methods that are not inherited from Object become implementations of MScript functions
                // with their name prefixed by '_':
                getOrCreateFunction(pluginName, "_" + method.getName()).addImplementation(method);

            }
        }
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
            // since we are scanning the plugins anyway, try to identify the one we might lookup into, at the same time:
            // NOTE: searching for the plugin could be faster if we modified the method signature to pass a map
            // associating a plugin id to its implementation!
            PluginAncestor plugin = null;
            if (availablePlugins != null) {
                for (PluginAncestor availablePlugin : availablePlugins) {
                    if (pluginName.equals(availablePlugin.getPluginID())) {
                        plugin = availablePlugin;
                        break;
                    }
                }
            }
            if (plugin == null) { // the plugin might not be available
                return new Lookup(PLUGIN_NOT_FOUND);
            }

            // Look up in the cache first since reflection-based lookup is slower:
            ConcurrentMap<String, Function> pluginFunctions = cache.get(pluginName);
            if (pluginFunctions != null) { // then assume the plugin has already been fully cached
                Function function = pluginFunctions.get(functionName);
                if (function != null) {

                }

            }

            // An alternative strategy is possible here: even if the plugin has already been cached but we can't find
            // the requested function either at all or with a corresponding implementation for the provided arity, we
            // can try to re-cache the plugin implementation, maybe the plugin list or object reference has dynamically
            // changed over the course of the program...

        }

        return new Lookup(FUNCTION_NOT_FOUND);
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
                throw new NullPointerException("lookup result cannot be null");
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