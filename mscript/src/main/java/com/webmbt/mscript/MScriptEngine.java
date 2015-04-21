package com.webmbt.mscript;

import com.webmbt.mscript.parse.MScriptLexer;
import com.webmbt.mscript.parse.MScriptParser;
import com.webmbt.plugin.MbtScriptExecutor;
import com.webmbt.plugin.PluginAncestor;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author TestOptimal, LLC
 * @version 1.0, Mar 06, 2015
 */
public class MScriptEngine {

    private Functions functions = new Functions();

    public MScriptEngine clearFunctionCache() {
        functions.clearCache();
        return this;
    }

    public List<MScriptError> checkMScript(String mScript, MbtScriptExecutor systemFunctions,
                                           List<PluginAncestor> availablePlugins) {
        if (mScript == null) {
            return Collections.emptyList();
        }

        List<MScriptError> mScriptErrors = new ArrayList<>();
        createParser(mScript, systemFunctions, availablePlugins, mScriptErrors).script();
        return mScriptErrors;
    }

    public String executeMScript(String mScriptExpression, MbtScriptExecutor systemFunctions,
                                 List<PluginAncestor> availablePlugins) throws Throwable {
        if (mScriptExpression == null) {
            return "";
        }

        // Parse the MScript source:
        List<MScriptError> mScriptErrors = new ArrayList<>();
        ParseTree mScriptParseTree =
            createParser(mScriptExpression, systemFunctions, availablePlugins, mScriptErrors).cond();

        // Handle / return any eventual parsing errors:
        if (!mScriptErrors.isEmpty()) {
            StringBuilder errors = new StringBuilder(mScriptErrors.get(0).toString());
            for (int i = 1; i < mScriptErrors.size(); i++) {
                errors.append(" ; ").append(mScriptErrors.get(i).toString());
            }
            return errors.toString();
        }

        // Execute / interpret the resulting MScript parse tree:
        try {
            return new MScriptEvalVisitor(systemFunctions, availablePlugins).visit(mScriptParseTree);
        } catch (Function.CallException ce) { // execution / interpretation stops at the first call exception
            Throwable cause = ce.getCause();
            throw cause == null ? ce : cause;
        }
    }

    protected MScriptParser createParser(String mScript, MbtScriptExecutor systemFunctions,
                                         List<PluginAncestor> availablePlugins, List<MScriptError> mScriptErrors) {
        MScriptLexer mScriptLexer = new MScriptLexer(new ANTLRInputStream(mScript));

        MScriptParser mScriptParser =
            new MScriptParser(new CommonTokenStream(mScriptLexer), functions, systemFunctions, availablePlugins);
        mScriptParser.addErrorListener(new MScriptErrorListener(mScript, mScriptErrors));
        return mScriptParser;
    }

    public static void main(String[] args) throws Throwable {
        MbtScriptExecutor systemFunctions = new MbtScriptExecutor();
        List<PluginAncestor> availablePlugins = new ArrayList<>();
        MScriptEngine mScriptEngine = new MScriptEngine();

        systemFunctions.setVar("v1", "123");
        systemFunctions.setVar("v2", "abc");

        String[] expressions = {//@fmt:off
            "1 < v1",
            "3 + v2",
            "1 > '02' - v1 * 1",
        };//@fmt:on

        for (String expression : expressions) {
            System.out.println(
                "RESULT: " + mScriptEngine.executeMScript(expression, systemFunctions, availablePlugins) + ".");
        }
    }
}
