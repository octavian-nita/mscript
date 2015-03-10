package com.webmbt.mscript;

import com.webmbt.plugin.MbtScriptExecutor;
import com.webmbt.plugin.PluginAncestor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Mar 06, 2015
 */
public class MScriptEngine {

    private MScriptParser mScriptParser = new MScriptParser();

    private Functions functions = new Functions();

    public List<MScriptError> checkMScript(String mScript, MbtScriptExecutor systemFunctions,
                                           List<PluginAncestor> plugins) {
        List<MScriptError> errors = new ArrayList<>();
        return errors;
    }

    public String executeMScript(String mScriptExpressions, MbtScriptExecutor systemFunctions,
                                 List<PluginAncestor> plugins) throws Exception {
        return "";
    }
}
