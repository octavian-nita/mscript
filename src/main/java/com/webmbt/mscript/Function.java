package com.webmbt.mscript;

/**
 * <p>
 * MScript function signatures can be inferred by scanning
 * {@link com.webmbt.plugin.MScriptInterface.MSCRIPT_METHOD}-annotated methods or
 * {@link com.webmbt.mscript.FunctionLibrary#load(String)} loaded} from {@link java.util.Properties} files.
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

    public final String qualifiedName;

    protected int minArity;

    protected int maxArity;

    public Function(String qualifiedName) {
        this(qualifiedName, 0, 0);
    }

    public Function(String qualifiedName, int arity) {
        this(qualifiedName, arity, arity);
    }

    public Function(String qualifiedName, int minArity, int maxArity) {
        if (qualifiedName == null) {
            throw new IllegalArgumentException("the (qualified) name of a function cannot be null");
        }
        if (!(qualifiedName = qualifiedName.trim()).startsWith("$")) {
            qualifiedName = "$" + qualifiedName;
        }
        if (qualifiedName.equals("$")) {
            throw new IllegalArgumentException("the (qualified) name of a function cannot be empty");
        }
        this.qualifiedName = qualifiedName;
        setMinArity(minArity);
        setMaxArity(maxArity);
    }

    public int getMinArity() {
        return minArity;
    }

    public void setMinArity(int minArity) {
        if (minArity < 0) {
            throw new IllegalArgumentException("the minimum arity of a function cannot be less than 0");
        }
        this.minArity = minArity;
    }

    public int getMaxArity() {
        return maxArity;
    }

    public void setMaxArity(int maxArity) {
        if (maxArity < minArity) {
            throw new IllegalArgumentException("the maximum arity of a function cannot be less than its minimum arity");
        }
        this.maxArity = maxArity;
    }

    @Override
    public String toString() {
        return qualifiedName + "(" + minArity + ".." + maxArity + ")";
    }
}
