package com.webmbt.mscript;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.util.logging.Level.WARNING;

/**
 * A library of defined functions.
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Dec 04, 2014
 */
public class FunctionLibrary {

    /**
     * {@link java.util.regex.Pattern Regular expression} used to parse a function's arity.
     */
    protected static final Pattern ARITY_PATTERN = Pattern.compile("(?:\\s*(\\d+)\\s*,)?\\s*(\\d+)\\s*");

    protected static final Logger logger = Logger.getLogger(FunctionLibrary.class.getName());

    protected final Map<String, Function> library = new ConcurrentHashMap<>();

    public void load(String libraryFilename) throws IOException {
        Properties signatures = new Properties();
        try (FileReader reader = new FileReader(libraryFilename)) {
            signatures.load(reader);
        } catch (IOException ioe) {     // then try to load the file from the classpath, as initially named
            InputStream resource = Function.class.getResourceAsStream(libraryFilename);
            if (resource == null) {     // then try to load the file from the classpath, as absolute path
                resource = Function.class.getResourceAsStream("/" + libraryFilename);
                if (resource == null) { // no such file in the classpath - just throw the initial exception
                    throw ioe;
                }
            }
            try {
                signatures.load(resource);
            } finally {
                try {
                    resource.close();
                } catch (IOException ioe2) {
                    logger.log(WARNING, "Cannot close function library definition resource stream; ignoring...", ioe2);
                }
            }
        }

        for (Map.Entry<Object, Object> signature : signatures.entrySet()) {
            Matcher arityMatcher = ARITY_PATTERN.matcher((String) signature.getValue());
            if (!arityMatcher.matches()) { // for now just skip the incorrectly defined functions but...
                logger.warning("Cannot parse arity for function " + signature.getKey() + "; skipping...");
                continue;
            }

            String minArity = arityMatcher.group(1);
            Function function =
                minArity == null ? new Function((String) signature.getKey(), parseInt(arityMatcher.group(2)))
                                 : new Function((String) signature.getKey(), parseInt(minArity),
                                                parseInt(arityMatcher.group(2)));

            library.put(function.qualifiedName, function);
        }
    }

    public void add(Function function) {
        library.put(function.qualifiedName, function);
    }

    public void clear() {
        library.clear();
    }

    /**
     * An MScript parser normally matches separately the plugin name, the function name and every passed argument; the
     * signature of this methods is designed so that it would be easily callable upon a successful function call match.
     */
    public CheckResult check(String pluginName, String functionName, int argsNumber) {
        StringBuilder qualifiedName = new StringBuilder(pluginName == null ? "" : pluginName.trim());
        if (qualifiedName.length() > 0) {
            qualifiedName.append('.');
        }
        qualifiedName.append(functionName == null ? "" : functionName.trim());
        if (qualifiedName.length() > 0 && qualifiedName.charAt(0) != '$') {
            qualifiedName.insert(0, '$');
        }

        Function function = library.get(qualifiedName.toString());

        if (function == null) {
            return CheckResult.NO_SUCH_FUNCTION;
        }

        if (argsNumber < function.minArity) {
            return CheckResult.TOO_FEW_ARGUMENTS;
        }

        if (argsNumber > function.maxArity) {
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
