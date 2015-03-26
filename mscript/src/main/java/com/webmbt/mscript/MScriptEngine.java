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

    private Functions functions = new Functions();

    /**
     * {@link org.antlr.v4.runtime.ANTLRErrorListener} that translates
     * {@link org.antlr.v4.runtime.RecognitionException}s to {@link MScriptError}s.
     */
    public static class MScriptErrorListener extends BaseErrorListener {

        private final String mScript;

        private final List<MScriptError> mScriptErrors; // where to accumulate errors for later reporting

        public MScriptErrorListener(String mScript, List<MScriptError> mScriptErrors) {
            if (mScriptErrors == null) {
                throw new IllegalArgumentException("cannot accumulate MScript errors in a null list");
            }
            this.mScript = mScript;
            this.mScriptErrors = mScriptErrors;
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

    public List<MScriptError> checkMScript(String mScript, MbtScriptExecutor systemFunctions,
                                           List<PluginAncestor> availablePlugins) {
        List<MScriptError> errors = new ArrayList<>();

        MScriptLexer mScriptLexer = new MScriptLexer(new ANTLRInputStream(mScript));

        MScriptParser mScriptParser =
            new MScriptParser(new CommonTokenStream(mScriptLexer), systemFunctions, availablePlugins);
        mScriptParser.addErrorListener(new MScriptErrorListener(mScript, errors));
        mScriptParser.script();

        return errors;
    }

    public String executeMScript(String mScriptExpressions, MbtScriptExecutor systemFunctions,
                                 List<PluginAncestor> availablePlugins) throws Exception { return ""; }
}
