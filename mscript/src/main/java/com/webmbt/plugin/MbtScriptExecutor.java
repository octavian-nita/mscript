package com.webmbt.plugin;

//!
//! TODO: THIS IS A STUB. PLEASE REPLACE WITH REAL CLASS WHEN INTEGRATING IN THE LARGER PROJECT!
//!

import java.util.HashMap;
import java.util.Map;

import static java.lang.Double.parseDouble;
import static java.lang.String.valueOf;

/**
 * Implements the MScript operators and system (built-in) functions.
 *
 * @author yxl01
 */
public class MbtScriptExecutor implements MScriptInterface {

    protected Map<String, String> symbolTable = new HashMap<>();

    @MSCRIPT_METHOD
    public void setVar(String name, String value) {
        symbolTable.put(name, value);
    }

    @MSCRIPT_METHOD
    public String getVar(String name) {
        return symbolTable.get(name);
    }

    @MSCRIPT_METHOD
    public String add(String val1, String val2) {
        try {
            return valueOf(parseDouble(val1) + parseDouble(val2));
        } catch (NumberFormatException nfe) { // if any of the conversions fail, just concatenate strings...
            return val1 + val2;
        }
    }

    @MSCRIPT_METHOD
    public String sub(String val1, String val2) {
        return valueOf(parseDouble(val1) - parseDouble(val2));
    }

    @MSCRIPT_METHOD
    public String mul(String val1, String val2) {
        return valueOf(parseDouble(val1) * parseDouble(val2));
    }

    @MSCRIPT_METHOD
    public String div(String val1, String val2) {
        return valueOf(parseDouble(val1) / parseDouble(val2));
    }

    @MSCRIPT_METHOD
    public String mod(String val1, String val2) {
        return valueOf(parseDouble(val1) % parseDouble(val2));
    }
}
