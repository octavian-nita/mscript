package com.webmbt.plugin;

/**
 * Implements the MScript system (built-in) functions.
 *
 * @author yxl01
 */
public class MbtScriptExecutor implements MScriptInterface {

    @MSCRIPT_METHOD
    public String f() { return "f()"; }

    @MSCRIPT_METHOD
    public int g(int arg0) { return 1; }

    @MSCRIPT_METHOD
    public int g(int arg0, String arg1) { return 2; }

    @MSCRIPT_METHOD
    public int g(int arg0, String arg1, float arg2, int arg3) { return 4; }

    @MSCRIPT_METHOD
    public String h(int arg0, double arg1) { return "2"; }

    @MSCRIPT_METHOD
    public int h(int arg0, String arg1, double arg2, String arg3, String arg4) { return 5; }
}
