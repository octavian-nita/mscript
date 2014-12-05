package com.webmbt.mscript;

/**
 * <p>
 * MScript function signatures can be inferred by scanning {@link com.webmbt.plugin.MScriptInterface.MSCRIPT_METHOD
 * MSCRIPT_METHOD}-annotated Java methods or {@link com.webmbt.mscript.FunctionLibrary#load(String) loaded} from {@link
 * java.util.Properties} files.
 * </p>
 * <p>
 * In MScript code, a function reference is prefixed by a sigil ($) and the name can optionally be prefixed by a
 * plugin name. MScript functions are <a href="http://en.wikipedia.org/wiki/Variadic_function">variadic</a>.
 * </p>
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.1, Dec 04, 2014
 */
public class Function {

    protected String pluginName;

    protected String name;

    protected int minArity;

    protected int maxArity;

    public Function(String name) {
        this(name, null, 0, 0);
    }

    public Function(String name, String pluginName) {
        this(name, pluginName, 0, 0);
    }

    public Function(String name, int arity) {
        this(name, null, arity, arity);
    }

    public Function(String name, int minArity, int maxArity) {
        this(name, null, minArity, maxArity);
    }

    public Function(String name, String pluginName, int arity) {
        this(name, pluginName, arity, arity);
    }

    public Function(String name, String pluginName, int minArity, int maxArity) {
        if (name == null || (name = name.trim()).length() == 0) {
            throw new IllegalArgumentException("the name of a function cannot be null or empty");
        }
        this.name = name;

        if (pluginName != null) {
            this.pluginName = pluginName.trim();
        }

        setMinArity(minArity);
        setMaxArity(maxArity);
    }

    public boolean isSystemFunction() {
        return pluginName == null || pluginName.trim().length() == 0;
    }

    public String getName() {
        return name;
    }

    public String getPluginName() {
        return pluginName;
    }

    public int getMinArity() {
        return minArity;
    }

    public Function setMinArity(int minArity) {
        if (minArity < 0) {
            throw new IllegalArgumentException("the minimum arity of a function cannot be less than 0");
        }
        this.minArity = minArity;
        return this;
    }

    public int getMaxArity() {
        return maxArity;
    }

    public Function setMaxArity(int maxArity) {
        if (maxArity < minArity) {
            throw new IllegalArgumentException("the maximum arity of a function cannot be less than its minimum arity");
        }
        this.maxArity = maxArity;
        return this;
    }

    @Override
    public String toString() {
        return (pluginName == null ? "$" : "$" + pluginName + ".") + name + "(" + minArity + ".." + maxArity + ")";
    }
}
