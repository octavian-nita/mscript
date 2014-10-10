package com.mscript;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.util.regex.Pattern.compile;

/**
 * MScript functions can optionally be prefixed by a plugin name and are <a href="http://en.wikipedia
 * .org/wiki/Variadic_function">variadic</a>. Function definitions can be loaded from {@link java.util.Properties}
 * files.
 */
public class Function {

    private static final Pattern ARITY_RE = compile("(?:\\s*(\\d+)\\s*,)?\\s*(\\d+)\\s*");

    private static final Map<String, Function> library = new HashMap<>();

    public static void loadLibrary(String libraryFilename) throws IOException {
        Properties definitions = new Properties();
        try (FileReader reader = new FileReader(libraryFilename)) {
            definitions.load(reader);
        }

        for (Map.Entry<Object, Object> definition : definitions.entrySet()) {
            String qualifiedName = (String) definition.getKey();
            if (!qualifiedName.startsWith("$")) { // simpler to just ensure the name starts with $...
                qualifiedName = "$" + qualifiedName;
            }

            Matcher arityMatcher = ARITY_RE.matcher((String) definition.getValue());
            if (!arityMatcher.matches()) { // for now just skip the incorrectly defined functions but...
                continue; // TODO: eventually log something before going to the next function definition
            }

            String minArity = arityMatcher.group(1);
            library.put(qualifiedName, minArity == null ? new Function(qualifiedName, parseInt(arityMatcher.group(2)))
                                                        : new Function(qualifiedName, parseInt(minArity),
                                                                       parseInt(arityMatcher.group(2))));
        }
    }

    public static Validation validate(String plugin, String name, int argsNumber) {
        StringBuilder qualifiedName = new StringBuilder(plugin == null ? "" : plugin.trim());
        if (qualifiedName.length() > 0) {
            qualifiedName.append('.');
        }
        qualifiedName.append(name == null ? "" : name.trim());
        if (qualifiedName.length() > 0 && qualifiedName.charAt(0) != '$') {
            qualifiedName.insert(0, '$');
        }
        Function function = library.get(qualifiedName.toString());

        if (function == null) {
            return Validation.NO_SUCH_FUNCTION;
        }

        if (argsNumber < function.minArity || argsNumber > function.maxArity) {
            return Validation.WRONG_NUM_OF_ARGS;
        }

        return Validation.OK;
    }

    public static enum Validation {
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
        this.maxArity = maxArity < this.minArity ? this.minArity : maxArity; // we might also want to issue a warning...
    }

    public Function(String qualifiedName, int minArity) {
        this(qualifiedName, minArity, minArity);
    }

    @Override
    public String toString() {
        return qualifiedName + "(" + minArity + ".." + maxArity + ")";
    }

    public static void main(String[] args) throws IOException {
        Function.loadLibrary("pluginFuncList.properties");

        for (Map.Entry<String, Function> e : Function.library.entrySet()) {
            System.out.println(e.getKey() + " :: " + e.getValue());
        }

        System.out.println(validate("web", "isPresent", 0));
        System.out.println(validate("web", "isPresent", 1));
        System.out.println(validate("web", "isPresent", 2));

        System.out.println(validate(null, "$rand", 0));
        System.out.println(validate(null, "$rand", 1));
        System.out.println(validate(null, "$rand", 2));

        System.out.println(validate(null, "foo", 2));
    }
}
