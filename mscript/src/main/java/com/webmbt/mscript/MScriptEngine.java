package com.webmbt.mscript;

import com.webmbt.mscript.parse.MScriptLexer;
import com.webmbt.mscript.parse.MScriptParser;
import com.webmbt.mscript.parse.MScriptParserBaseVisitor;
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
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Mar 06, 2015
 */
public class MScriptEngine {

    public List<MScriptError> checkMScript(String mScript, MbtScriptExecutor systemFunctions,
                                           List<PluginAncestor> availablePlugins) {
        List<MScriptError> mScriptErrors = new ArrayList<>();
        createParser(mScript, systemFunctions, availablePlugins, mScriptErrors).script();
        return mScriptErrors;
    }

    public String executeMScript(String mScriptExpression, MbtScriptExecutor systemFunctions,
                                 List<PluginAncestor> availablePlugins) throws Exception {
        if (mScriptExpression == null) {
            return "";
        }

        List<MScriptError> mScriptErrors = new ArrayList<>();
        ParseTree mScriptParseTree =
            createParser(mScriptExpression, systemFunctions, availablePlugins, mScriptErrors).cond();

        if (!mScriptErrors.isEmpty()) {
            StringBuilder errors = new StringBuilder(mScriptErrors.get(0).toString());
            for (int i = 1; i < mScriptErrors.size(); i++) {
                errors.append(" ; ").append(mScriptErrors.get(i).toString());
            }
            return errors.toString();
        }

        return new MScriptEvalVisitor().visit(mScriptParseTree);
    }

    protected MScriptParser createParser(String mScript, MbtScriptExecutor systemFunctions,
                                         List<PluginAncestor> availablePlugins, List<MScriptError> mScriptErrors) {
        MScriptLexer mScriptLexer = new MScriptLexer(new ANTLRInputStream(mScript));

        MScriptParser mScriptParser =
            new MScriptParser(new CommonTokenStream(mScriptLexer), functions, systemFunctions, availablePlugins);
        mScriptParser.addErrorListener(new MScriptErrorListener(mScript, mScriptErrors));
        return mScriptParser;
    }

    /**
     * {@link org.antlr.v4.runtime.ANTLRErrorListener} that translates
     * {@link org.antlr.v4.runtime.RecognitionException}s to {@link MScriptError}s.
     */
    public static class MScriptErrorListener extends BaseErrorListener {

        private final String mScript;

        private final List<MScriptError> mScriptErrors; // where to accumulate errors for later reporting

        public MScriptErrorListener(String mScript, List<MScriptError> mScriptErrors) {
            if (mScriptErrors == null) {
                throw new IllegalArgumentException("the list to accumulate MScript errors canot be null");
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

    public static class MScriptEvalVisitor extends MScriptParserBaseVisitor<String> {

        @Override
        public String visitCond(@NotNull MScriptParser.CondContext ctx) {
            return "";
        }
    }

    private Functions functions = new Functions();

    public MScriptEngine clearFunctionCache() {
        functions.clearCache();
        return this;
    }
}
