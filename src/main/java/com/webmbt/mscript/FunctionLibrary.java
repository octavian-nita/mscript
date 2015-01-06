package com.webmbt.mscript;

import com.webmbt.plugin.PluginAncestor;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.util.logging.Level.WARNING;

/**
 * <p>
 * A symbol table for MScript {@link Function function definitions}. At least for testing purposes definitions
 * without implementations can be loaded from {@link Properties} {@link #load(java.util.Properties) instances}
 * or {@link #load(String) files}.
 * </p>
 * <p>
 * When looking up a function, a filtering list of plugins can be provided in order to restrict the lookup. If looking
 * up in the internal cache fails, the provided <em>system functions</em> instance and plugins are scanned for {@link
 * MScriptInterface.MSCRIPT_METHOD}-annotated and public methods and if found, the definition is cached and retrieved.
 * </p>
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Dec 04, 2014
 */
public class FunctionLibrary {

    /**
     * Key to group the <em>{@link Function#isSystemFunction() system}</em> functions symbol (sub)table.
     */
    protected static final String SYSTEM_FUNCTIONS = "__SYS__";

    protected final ConcurrentMap<String, ConcurrentMap<String, Function>> library = new ConcurrentHashMap<>();

    public FunctionLibrary load(String libraryFilename) throws IOException {
        Class<?> klass = getClass();
        Logger log = Logger.getLogger(klass.getName());

        Properties signatures = new Properties();
        try (FileReader reader = new FileReader(libraryFilename)) {
            signatures.load(reader);
            log.info("Loaded function signatures from file " + libraryFilename);
        } catch (IOException ioe) {     // try to load the file from the classpath, as initially specified
            InputStream resource = klass.getResourceAsStream(libraryFilename);
            if (resource == null) {     // try to load the file from the classpath, as absolute path
                resource = klass.getResourceAsStream(libraryFilename = "/" + libraryFilename);
                if (resource == null) { // no such file in the classpath, just throw the initial exception
                    throw ioe;
                }
            }
            try {
                signatures.load(resource);
                log.info("Loaded function signatures from classpath resource " + libraryFilename);
            } finally {
                try {
                    resource.close();
                } catch (IOException ioe2) {
                    log.log(WARNING, "Cannot close function library definition resource stream; ignoring...", ioe2);
                }
            }
        }

        return load(signatures);
    }

    public FunctionLibrary load(Properties signatures) {
        if (signatures == null) {
            throw new NullPointerException("cannot load function library from a null signatures specification");
        }

        Logger log = Logger.getLogger(getClass().getName());
        Pattern namesPattern = Pattern.compile("\\s*\\$?(?:([a-zA-Z_][a-zA-Z_0-9]*)\\.)?([a-zA-Z_][a-zA-Z_0-9]*)\\s*");
        Pattern arityPattern = Pattern.compile("(?:\\s*(\\d+)\\s*,)?\\s*(\\d+)\\s*");

        for (ConcurrentMap.Entry<Object, Object> signature : signatures.entrySet()) {
            try {
                Matcher namesMatcher = namesPattern.matcher((String) signature.getKey());
                if (!namesMatcher.matches()) { // skip the incorrectly defined functions...
                    log.log(WARNING, "Cannot parse function plugin and/or name for {0}; skipping...",
                            signature.getKey());
                    continue;
                }

                Matcher arityMatcher = arityPattern.matcher((String) signature.getValue());
                if (!arityMatcher.matches()) { // skip the incorrectly defined functions...
                    log.log(WARNING, "Cannot parse function arity for {0}; skipping...", signature.getKey());
                    continue;
                }

                String name = namesMatcher.group(2);
                String plugin = namesMatcher.group(1);
                String minArity = arityMatcher.group(1);
                Function function = minArity == null ? new Function(name, plugin, parseInt(arityMatcher.group(2)))
                                                     : new Function(name, plugin, parseInt(minArity),
                                                                    parseInt(arityMatcher.group(2)));

                add(function);
            } catch (Throwable throwable) {
                log.log(WARNING, "Cannot parse function signature for " + signature.getKey() + "; skipping...",
                        throwable);
            }
        }

        return this;
    }

    public FunctionLibrary add(Function function) {
        if (function == null) {
            throw new NullPointerException("cannot add a null function to a library");
        }

        // Under which plugin to store the function?
        String pluginName = function.isSystemFunction() ? SYSTEM_FUNCTIONS : function.getPluginName();

        // See
        // http://stackoverflow.com/questions/10743622/concurrenthashmap-avoid-extra-object-creation-with-putifabsent
        // for an explanation as to why the following is acceptable from a thread-safety point of view:
        ConcurrentMap<String, Function> pluginFunctions = library.get(pluginName);
        if (pluginFunctions == null) {
            library.putIfAbsent(pluginName, new ConcurrentHashMap<String, Function>());
            pluginFunctions = library.get(pluginName);
        }
        pluginFunctions.put(function.getName(), function);

        return this;
    }

    public FunctionLibrary add(String pluginName, String functionName, Method method) {
        if (method == null) {
            throw new NullPointerException("cannot add a function with a null (Java) implementation to a library");
        }
        if (functionName == null || (functionName = functionName.trim()).length() == 0) {
            throw new IllegalArgumentException("cannot add a function with a null or empty name to a library");
        }
        if (pluginName == null || (pluginName = pluginName.trim()).length() == 0) {
            pluginName = SYSTEM_FUNCTIONS;
        }

        ConcurrentMap<String, Function> pluginFunctions = library.get(pluginName);
        if (pluginFunctions == null) {
            pluginFunctions = new ConcurrentHashMap<>();
            library.put(pluginName, pluginFunctions);
        }

        Function function = pluginFunctions.get(functionName);
        if (function == null) {
            pluginFunctions.put(functionName,
                                new Function(functionName, SYSTEM_FUNCTIONS.equals(pluginName) ? null : pluginName)
                                    .addImplementation(method));
        } else {
            function.addImplementation(method);
        }

        return this;
    }

    public LookupResult lookup(String pluginName, String functionName, int argsNumber) {
        return lookup(pluginName, functionName, argsNumber, null);
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

        Set<String> pluginNames = new TreeSet<>(); // for faster look-ups
        if (plugins != null) {
            for (PluginAncestor plugin : plugins) {
                if (plugin != null) {
                    pluginNames.add(plugin.getPluginID());
                }
            }
        }

        if (pluginName != null && (pluginName = pluginName.trim()).length() > 0) {
            if (!pluginNames.contains(pluginName)) {
                return LookupResult.PLUGIN_NOT_ACCESSIBLE;
            }

            ConcurrentMap<String, Function> pluginFunctions = library.get(pluginName);
            if (pluginFunctions == null) {
                return LookupResult.PLUGIN_NOT_FOUND;
            }

            return checkFunction(pluginFunctions.get(functionName), argsNumber);
        }

        // No plugin name has been provided: check the available system functions and fall back on the provided plugins:
        ConcurrentMap<String, Function> systemFunctions = library.get(SYSTEM_FUNCTIONS);
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
