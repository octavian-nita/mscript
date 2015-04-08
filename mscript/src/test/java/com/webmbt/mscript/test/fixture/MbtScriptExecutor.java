package com.webmbt.mscript.test.fixture;

import static java.lang.Integer.parseInt;

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
    public String h(String param0, String param1, String param2) { return "3"; }

    @MSCRIPT_METHOD
    public int h(String param0, String param1, String param2, String param3, String param4) { return 5; }

    @MSCRIPT_METHOD
    public String getData(String dsName, String fieldName) { return "xy"; }

    @MSCRIPT_METHOD
    public double rand() { return Math.random(); }

    @MSCRIPT_METHOD
    public int rand(String range) { return (int) (Math.random() * parseInt(range)); }

    @MSCRIPT_METHOD
    public int size(String arg) { return arg == null ? 0 : arg.length(); }

    @MSCRIPT_METHOD
    public boolean not(String arg) { return false; }

    @MSCRIPT_METHOD
    public boolean isEven(String arg) { return false; }

    @MSCRIPT_METHOD
    public int increment(String arg) { return 0; }
}
