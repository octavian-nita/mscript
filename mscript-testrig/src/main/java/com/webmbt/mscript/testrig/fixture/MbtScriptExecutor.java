package com.webmbt.mscript.testrig.fixture;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;

/** Implements the MScript system (built-in) functions. */
public class MbtScriptExecutor extends com.webmbt.plugin.MbtScriptExecutor {

    @MSCRIPT_METHOD
    public String f() { return "f()"; }

    @MSCRIPT_METHOD
    public String g(String param0) { return "1"; }

    @MSCRIPT_METHOD
    public int g(String param0, String param1) { return 2; }

    @MSCRIPT_METHOD
    public int g(String param0, String param1, String param2, String param3) { return 4; }

    @MSCRIPT_METHOD
    public String h(String param0, String param1) { return "2"; }

    @MSCRIPT_METHOD
    public int h(String param0, String param1, String param2, String param3, String param4) { return 5; }

    @MSCRIPT_METHOD
    public String add(String val1, String val2) {
        try {
            return valueOf(parseDouble(val1) + parseDouble(val2));
        } catch (NumberFormatException nfe) { // if any of the conversions fail, just concatenate strings...
            return val1 + val2;
        }
    }

    @MSCRIPT_METHOD
    public String subtract(String val1, String val2) {
        return valueOf(parseDouble(val1) - parseDouble(val2));
    }

    @MSCRIPT_METHOD
    public String multiply(String val1, String val2) {
        return valueOf(parseDouble(val1) * parseDouble(val2));
    }

    @MSCRIPT_METHOD
    public String divide(String val1, String val2) {
        return valueOf(parseDouble(val1) / parseDouble(val2));
    }

    @MSCRIPT_METHOD
    public String mod(String val1, String val2) {
        return valueOf(parseDouble(val1) % parseDouble(val2));
    }

    @MSCRIPT_METHOD
    public String setVar(String varName, String varVal) {
        return "";
    }

    /**
     * Variable retrieval: $isPresent('[var1]') => $isPresent($getVar('var1')).
     * Is there a better way to reference var? Like $isPresent($var1)...
     */
    @MSCRIPT_METHOD
    public String getVar(String varName) {
        return "123";
    }

    @MSCRIPT_METHOD
    public String getData(String dsName, String fieldName) {
        return "xy";
    }

    @MSCRIPT_METHOD
    public double rand() {
        return Math.random();
    }

    @MSCRIPT_METHOD
    public int rand(String range) {
        return (int) (Math.random() * parseInt(range));
    }
}
