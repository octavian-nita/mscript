package com.webmbt.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.webmbt.mscript.Types.asNumber;
import static com.webmbt.mscript.Types.asString;
import static java.util.logging.Level.WARNING;

/**
 * Implements the MScript operators and system (built-in) functions.
 * TODO: THIS IS A STUB. PLEASE REPLACE WITH REAL CLASS WHEN INTEGRATING IN THE LARGER PROJECT!
 *
 * @author yxl01
 */
public class MbtScriptExecutor implements MScriptInterface {

    protected static final Logger LOG = Logger.getLogger(MbtScriptExecutor.class.getName());

    protected Map<String, String> symbolTable = new HashMap<>();

    @MSCRIPT_METHOD
    public void setVar(String name, String value) {
        symbolTable.put(name, value);
    }

    @MSCRIPT_METHOD
    public String getVar(String name) {
        if (!symbolTable.containsKey(name)) {
            LOG.log(WARNING, "Accessing undefined variable: " + name);
        }
        return symbolTable.get(name);
    }

    @MSCRIPT_METHOD
    public String add(String val1, String val2) {
        try {
            return asString(asNumber(val1) + asNumber(val2));
        } catch (NumberFormatException nfe) { // if any of the conversions fail, just concatenate strings...
            return val1 + val2;
        }
    }

    @MSCRIPT_METHOD
    public String sub(String val1, String val2) {
        return asString(asNumber(val1) - asNumber(val2));
    }

    @MSCRIPT_METHOD
    public String mul(String val1, String val2) {
        return asString(asNumber(val1) * asNumber(val2));
    }

    @MSCRIPT_METHOD
    public String div(String val1, String val2) {
        return asString(asNumber(val1) / asNumber(val2));
    }

    @MSCRIPT_METHOD
    public String mod(String val1, String val2) {
        return asString(asNumber(val1) % asNumber(val2));
    }
}
