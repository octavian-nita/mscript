package com.mscript;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class Function {

    private static final Map<String, Function> library = new HashMap<>();

    public static void loadLibrary(String libraryFilename) throws IOException {
        Properties definitions = new Properties();
        try (FileReader reader = new FileReader(libraryFilename)) {
            definitions.load(reader);
        }

        for (Map.Entry<Object, Object> definition : definitions.entrySet()) {
            String nameOrQualifiedName = (String) definition.getKey();

            Matcher arityMatcher = ARITY_RE.matcher((String) definition.getValue());
            if (!arityMatcher.matches()) { // for now, just skip poorly defined functions, but...
                continue; // TODO: eventually log something before skipping to the next function definition...
            }
            System.out.println(arityMatcher.group(0));
            String maxArity = arityMatcher.group(2);
            library.put(nameOrQualifiedName,
                        maxArity == null ? new Function(nameOrQualifiedName, parseInt(arityMatcher.group(1)))
                                         : new Function(nameOrQualifiedName, parseInt(arityMatcher.group(1)),
                                                        parseInt(maxArity)));
        }
    }

    public static void clearLibrary() { library.clear(); }

    private static final Pattern FULLY_QUALIFIED_NAME_RE =
        Pattern.compile("\\s*\\$?(?:([a-zA-Z_][a-zA-Z0-9_]*)\\.)?([a-zA-Z_][a-zA-Z0-9_]*)\\s*");

    private static final Pattern ARITY_RE = Pattern.compile("(?:\\s*(\\d+)\\s*,)?\\s*(\\d+)\\s*");

    public final String qualifiedName;

    public final String plugin;

    public final String name;

    public final int minArity;

    public final int maxArity;

    public Function(String plugin, String name, int minArity, int maxArity) {
        if (name == null || (name = name.trim()).length() == 0) {
            throw new IllegalArgumentException("the name of a function cannot be null or empty");
        }
        this.name = name;

        if (plugin == null || (plugin = plugin.trim()).length() == 0) {
            this.plugin = "";
            this.qualifiedName = this.name;
        } else {
            this.plugin = plugin;
            this.qualifiedName = this.plugin + "." + this.name;
        }

        if (minArity < 0) {
            throw new IllegalArgumentException("the minimum arity of a function cannot be less than 0");
        }
        this.minArity = minArity;
        this.maxArity = maxArity < this.minArity ? this.minArity : maxArity; // we might also want to issue a warning...
    }

    public Function(String plugin, String name, int minArity) {
        this(plugin, name, minArity, minArity);
    }

    public Function(String plugin, String name) {
        this(plugin, name, 0, 0);
    }

    public Function(String nameOrQualifiedName, int minArity, int maxArity) {
        if (nameOrQualifiedName == null) {
            throw new IllegalArgumentException("the name or qualified name of a function cannot be null or empty");
        }
        Matcher qNameMatcher = FULLY_QUALIFIED_NAME_RE.matcher(nameOrQualifiedName);
        if (!qNameMatcher.matches()) {
            throw new IllegalArgumentException(
                "the name or qualified name of a function should match the [plugin.]function pattern");
        }

        this.qualifiedName = nameOrQualifiedName;
        String name = qNameMatcher.group(2);
        if (name == null) {
            this.plugin = "";
            this.name = qNameMatcher.group(1);
        } else {
            this.plugin = qNameMatcher.group(1);
            this.name = name;
        }

        if (minArity < 0) {
            throw new IllegalArgumentException("the minimum arity of a function cannot be less than 0");
        }
        this.minArity = minArity;
        this.maxArity = maxArity < this.minArity ? this.minArity : maxArity; // we might also want to issue a warning...
    }

    public Function(String nameOrQualifiedName, int minArity) {
        this(nameOrQualifiedName, minArity, minArity);
    }

    public Function(String nameOrQualifiedName) {
        this(nameOrQualifiedName, 0, 0);
    }

    @Override
    public String toString() {
        return "$" + qualifiedName + "(" + minArity + ".." + maxArity + ")";
    }

    public static void main(String[] args) throws IOException {
        Function.loadLibrary("pluginFuncList.properties");

        System.out.println(Function.library);
    }
}
