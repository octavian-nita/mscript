package com.webmbt.mscript;

import com.webmbt.plugin.PluginAncestor;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.util.logging.Level.WARNING;

/**
 * <p>
 * A library of predefined MScript {@link Function functions}, grouped by {@link Function#getPluginName() plugin names},
 * having the <em>{@link Function#isSystemFunction() system}</em> functions under the {@link #SYSTEM_FUNCTIONS} name.
 * </p>
 * <p>
 * MScript function signatures can be inferred by scanning {@link com.webmbt.plugin.MScriptInterface.MSCRIPT_METHOD
 * MSCRIPT_METHOD}-annotated Java methods or loaded from {@link Properties} {@link #load(Properties) instances} or
 * {@link #load(String) files}.
 * </p>
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Dec 04, 2014
 */
public class FunctionLibrary {

    /**
     * Key for the <em>{@link Function#isSystemFunction() system}</em> functions symbol (sub)table.
     */
    protected static final String SYSTEM_FUNCTIONS = "__SYS__";

    protected final ConcurrentMap<String, ConcurrentMap<String, Function>> library = new ConcurrentHashMap<>();

    public void load(String libraryFilename) throws IOException {
        Class<?> klass = getClass();

        Properties signatures = new Properties();
        try (FileReader reader = new FileReader(libraryFilename)) {
            signatures.load(reader);
        } catch (IOException ioe) {     // try to load the file from the classpath, as initially specified
            InputStream resource = klass.getResourceAsStream(libraryFilename);
            if (resource == null) {     // try to load the file from the classpath, as absolute path
                resource = klass.getResourceAsStream("/" + libraryFilename);
                if (resource == null) { // no such file in the classpath, just throw the initial exception
                    throw ioe;
                }
            }
            try {
                signatures.load(resource);
            } finally {
                try {
                    resource.close();
                } catch (IOException ioe2) {
                    Logger.getLogger(klass.getName())
                          .log(WARNING, "cannot close function library definition resource stream; ignoring...", ioe2);
                }
            }
        }

        load(signatures);
    }

    public void load(Properties signatures) {
        if (signatures == null) {
            throw new NullPointerException("cannot load function library from a null signatures specification");
        }

        Logger log = Logger.getLogger(getClass().getName());
        Pattern namesPattern = Pattern.compile("\\s*\\$?(?:([a-zA-Z_][a-zA-Z_0-9]*)\\.)?([a-zA-Z_][a-zA-Z_0-9]*)\\s*");
        Pattern arityPattern = Pattern.compile("(?:\\s*(\\d+)\\s*,)?\\s*(\\d+)\\s*");

        for (ConcurrentMap.Entry<Object, Object> signature : signatures.entrySet()) {
            try {
                Matcher namesMatcher = namesPattern.matcher((String) signature.getKey());
                if (!namesMatcher.matches()) { // for now just skip the incorrectly defined functions...
                    log.warning("cannot parse function plugin and/or name for " + signature.getKey() + "; skipping...");
                    continue;
                }

                Matcher arityMatcher = arityPattern.matcher((String) signature.getValue());
                if (!arityMatcher.matches()) { // for now just skip the incorrectly defined functions...
                    log.warning("cannot parse function arity for " + signature.getKey() + "; skipping...");
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
                log.log(WARNING, "cannot parse function signature for " + signature.getKey() + "; skipping...",
                        throwable);
            }
        }
    }

    public void add(Function function) {
        if (function == null) {
            throw new NullPointerException("cannot add a null function to a library");
        }

        String plugin = function.isSystemFunction() ? SYSTEM_FUNCTIONS : function.getPluginName();

        // Not exactly thread-safe but the assumption is that we build / add to the functions library once, at the
        // beginning of the process. We could use ConcurrentMap.putIfAbsent() but then we would create (local but)
        // unnecessary ConcurrentHashMap instances.
        ConcurrentMap<String, Function> pluginFunctions = library.get(plugin);
        if (pluginFunctions == null) {
            pluginFunctions = new ConcurrentHashMap<>();
            library.put(plugin, pluginFunctions);
        }

        pluginFunctions.put(function.getName(), function);
    }

    public CheckResult check(String pluginName, String functionName, int argsNumber) {
        return check(pluginName, functionName, argsNumber, null);
    }

    /**
     * An MScript parser normally matches separately the plugin name, the function name and every passed argument; the
     * signature of this methods is designed so that it would be easily callable upon a successful function call match.
     *
     * @param plugins if non-<code>null</code> and if a function without a plugin name is not found under system
     *                functions, the provided plugins are checked in the provided order
     */
    public CheckResult check(String pluginName, String functionName, int argsNumber, List<PluginAncestor> plugins) {
        if (pluginName == null || (pluginName = pluginName.trim()).length() == 0) {
            pluginName = SYSTEM_FUNCTIONS;

        } else {

        }

        ConcurrentMap<String, Function> functions = library.get(pluginName);
        if (functions == null) {
            return CheckResult.PLUGIN_NOT_FOUND;
        }

        Function function = functions.get(functionName);
        out:
        if (function == null) {
            if (plugins != null) {
                for (PluginAncestor plugin : plugins) {
                    if (plugin != null) {
                        functions = library.get(plugin.getPluginID());
                        if (functions != null) {
                            function = functions.get(functionName);
                            if (function != null) {
                                break out;
                            }
                        }
                    }
                }
            }

            return CheckResult.FUNCTION_NOT_FOUND;
        }

        if (argsNumber < function.getMinArity()) {
            return CheckResult.TOO_FEW_ARGUMENTS;
        }

        if (argsNumber > function.getMaxArity()) {
            return CheckResult.TOO_MANY_ARGUMENTS;
        }

        return CheckResult.OK;
    }

    public static enum CheckResult {

        OK,

        PLUGIN_NOT_FOUND,
        PLUGIN_NOT_ACCESSIBLE,

        FUNCTION_NOT_FOUND,
        TOO_MANY_ARGUMENTS,
        TOO_FEW_ARGUMENTS;

        @Override
        public String toString() {
            return super.toString().toLowerCase().replaceAll("_+", ".");
        }
    }
}
