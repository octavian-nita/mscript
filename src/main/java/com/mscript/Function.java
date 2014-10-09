package com.mscript;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Function {

    private static final Map<String, Function> library = new HashMap<>();

    public static void loadLibrary(String libraryFilename) throws IOException {
        Properties definitions = new Properties();
        try (FileReader reader = new FileReader(libraryFilename)) {
            definitions.load(reader);
        }

        for (Map.Entry<Object, Object> definition : definitions.entrySet()) {
            String nameOrQualifiedName = (String) definition.getKey();

            String arity = (String) definition.getValue();

            // parse the arity and create a function object...
        }
    }

    public static void clearLibrary() {
        library.clear();
    }

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
        Matcher matcher = FULLY_QUALIFIED_NAME_RE.matcher(nameOrQualifiedName);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                "the name or qualified name of a function should match the [plugin.]function pattern");
        }

        this.qualifiedName = nameOrQualifiedName;
        if (matcher.groupCount() == 1) {
            this.plugin = "";
            this.name = matcher.group(1);
        } else { // can only be 2...
            this.plugin = matcher.group(1);
            this.name = matcher.group(2);
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
}
