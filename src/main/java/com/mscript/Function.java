package com.mscript;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

/**
 * MScript function definitions are {@link #loadLibrary(String) loaded} from {@link java.util.Properties} files. A
 * function reference is prefixed by a sigil ($) and the name can optionally be prefixed by a plugin name. MScript
 * functions are <a href="http://en.wikipedia.org/wiki/Variadic_function">variadic</a>.
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Oct 21, 2014
 */
public class Function {

    /**
     * {@link Pattern Regular expression} used to parse a function's arity.
     */
    protected static final Pattern ARITY_PATTERN = Pattern.compile("(?:\\s*(\\d+)\\s*,)?\\s*(\\d+)\\s*");

    protected static final Map<String, Function> library = new HashMap<>();

    public static void define(String qualifiedName, int minArity, int maxArity) {
        Function function = new Function(qualifiedName, minArity, maxArity);
        library.put(function.qualifiedName, function);
    }

    public static void define(String qualifiedName, int arity) {
        define(qualifiedName, arity, arity);
    }

    public static void clearLibrary() {
        library.clear();
    }

    public static void loadLibrary(String libraryFilename) throws IOException {
        Properties definitions = new Properties();
        try (FileReader reader = new FileReader(libraryFilename)) {
            definitions.load(reader);
        }

        for (Map.Entry<Object, Object> definition : definitions.entrySet()) {
            Matcher arityMatcher = ARITY_PATTERN.matcher((String) definition.getValue());
            if (!arityMatcher.matches()) { // for now just skip the incorrectly defined functions but...
                continue; // TODO: eventually log something before going to the next function definition
            }

            String minArity = arityMatcher.group(1);
            Function function =
                minArity == null ? new Function((String) definition.getKey(), parseInt(arityMatcher.group(2)))
                                 : new Function((String) definition.getKey(), parseInt(minArity),
                                                parseInt(arityMatcher.group(2)));

            library.put(function.qualifiedName, function);
        }
    }

    /**
     * An MScript parser normally matches separately the plugin, the function name and every passed argument so the
     * signature of this methods makes it easy to be called upon a successful function call match.
     */
    public static CheckResult check(String pluginName, String functionName, int argsNumber) {
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
        if (argsNumber < function.minArity || argsNumber > function.maxArity) {
            return CheckResult.WRONG_NUM_OF_ARGS;
        }
        return CheckResult.OK;
    }

    // One might consider refactoring this enum into an actual class and associate a particular error message with every
    // performed {@link #check(String, String, int)}.
    public static enum CheckResult {
        OK,
        NO_SUCH_FUNCTION,
        WRONG_NUM_OF_ARGS
    }

    public final String qualifiedName;

    public final int minArity;

    public final int maxArity;

    public Function(String qualifiedName, int minArity, int maxArity) {
        if (qualifiedName == null) {
            throw new IllegalArgumentException("the (qualified) name of a function cannot be null");
        }
        if (!qualifiedName.startsWith("$")) {
            qualifiedName = "$" + qualifiedName;
        }
        if (qualifiedName.equals("$")) {
            throw new IllegalArgumentException("the (qualified) name of a function cannot be empty");
        }
        this.qualifiedName = qualifiedName;

        if (minArity < 0) {
            throw new IllegalArgumentException("the minimum arity of a function cannot be less than 0");
        }
        this.minArity = minArity;

        if (maxArity < minArity) {
            throw new IllegalArgumentException("the maximum arity of a function cannot be less than its minimum arity");
        }
        this.maxArity = maxArity;
    }

    public Function(String qualifiedName, int arity) {
        this(qualifiedName, arity, arity);
    }

    @Override
    public String toString() {
        return qualifiedName + "(" + minArity + ".." + maxArity + ")";
    }
}
