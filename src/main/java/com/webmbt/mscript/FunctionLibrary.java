package com.webmbt.mscript;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.util.logging.Level.WARNING;

/**
 * A library of defined MScript functions, grouped by plugin names and having the 'system' functions grouped under
 * the {@link #SYSTEM_FUNCTIONS} name/key.
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Dec 04, 2014
 */
public class FunctionLibrary {

    /**
     * Key for the 'system' functions symbol (sub)table.
     */
    protected static final String SYSTEM_FUNCTIONS = "__SYS__";

    protected final ConcurrentMap<String, ConcurrentMap<String, Function>> library = new ConcurrentHashMap<>();

    public void load(String libraryFilename) throws IOException {
        Class<?> klass = getClass();
        Logger log = Logger.getLogger(klass.getName());

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
                    log.log(WARNING, "cannot close function library definition resource stream; ignoring...", ioe2);
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
                log.log(WARNING, "cannot parse function signature for " + signature.getValue() + "; skipping...",
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

    /**
     * An MScript parser normally matches separately the plugin name, the function name and every passed argument; the
     * signature of this methods is designed so that it would be easily callable upon a successful function call match.
     */
    public CheckResult check(String pluginName, String functionName, int argsNumber) {
        if (pluginName == null || (pluginName = pluginName.trim()).length() == 0) {
            pluginName = SYSTEM_FUNCTIONS;
        }

        ConcurrentMap<String, Function> functions = library.get(pluginName);
        if (functions == null) {
            return CheckResult.NO_SUCH_FUNCTION;
        }

        Function function = functions.get(functionName);
        if (function == null) {
            return CheckResult.NO_SUCH_FUNCTION;
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
        NO_SUCH_FUNCTION,
        TOO_FEW_ARGUMENTS,
        TOO_MANY_ARGUMENTS
    }
}
