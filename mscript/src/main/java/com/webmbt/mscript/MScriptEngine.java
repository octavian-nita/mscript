package com.webmbt.mscript;

import com.webmbt.mscript.parse.MScriptLexer;
import com.webmbt.mscript.parse.MScriptParser;
import com.webmbt.mscript.parse.MScriptRecognitionException;
import com.webmbt.plugin.MbtScriptExecutor;
import com.webmbt.plugin.PluginAncestor;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Mar 06, 2015
 */
public class MScriptEngine {

    /**
     * {@link org.antlr.v4.runtime.ANTLRErrorListener} that translates
     * {@link org.antlr.v4.runtime.RecognitionException}s to {@link MScriptError}s.
     */
    public static class MScriptErrorListener extends BaseErrorListener {

        private final String mScript;

        private final List<MScriptError> mScriptErrors; // where to accumulate errors for later reporting

        public List<MScriptError> getErrors() { return mScriptErrors; }

        public MScriptErrorListener(String mScript) { this(mScript, null); }

        public MScriptErrorListener(String mScript, List<MScriptError> mScriptErrors) {
            this.mScript = mScript;
            this.mScriptErrors = mScriptErrors == null ? new ArrayList<MScriptError>() : mScriptErrors;
        }

        @Override
        public <T extends Token> void syntaxError(@NotNull Recognizer<T, ?> recognizer, @Nullable T offendingSymbol,
                                                  int line, int charPositionInLine, @NotNull String message,
                                                  @Nullable RecognitionException exception) {
            if (exception instanceof MScriptRecognitionException) {
                MScriptRecognitionException mScriptEx = (MScriptRecognitionException) exception;
                mScriptErrors.add(new MScriptError(mScript, offendingSymbol.getText(), line, charPositionInLine,
                                                   mScriptEx.getErrorCode(), mScriptEx.getErrorArguments()));
            } else {
                mScriptErrors.add(
                    new MScriptError(mScript, offendingSymbol.getText(), line, charPositionInLine, "E_PARSE",
                                     message == null ? exception.getMessage() : message));
            }
        }
    }

    private Functions functions = new Functions();

    public MScriptEngine clearFunctionCache() {
        functions.clearCache();
        return this;
    }

    public List<MScriptError> checkMScript(String mScript, MbtScriptExecutor systemFunctions,
                                           List<PluginAncestor> availablePlugins) {
        MScriptLexer mScriptLexer = new MScriptLexer(new ANTLRInputStream(mScript));

        MScriptParser mScriptParser =
            new MScriptParser(new CommonTokenStream(mScriptLexer), functions, systemFunctions, availablePlugins);

        MScriptErrorListener mScriptErrorListener = new MScriptErrorListener(mScript);

        mScriptParser.addErrorListener(mScriptErrorListener);
        mScriptParser.script();
        return mScriptErrorListener.getErrors();
    }

    public String executeMScript(String mScriptExpressions, MbtScriptExecutor systemFunctions,
                                 List<PluginAncestor> availablePlugins) throws Exception { return ""; }
}
