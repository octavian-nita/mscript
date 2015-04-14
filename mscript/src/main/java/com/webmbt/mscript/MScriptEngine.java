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
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.webmbt.mscript.Functions.Lookup;
import static com.webmbt.mscript.Functions.Lookup.Result.FOUND;
import static com.webmbt.mscript.parse.MScriptLexer.IN_STR_LBRACK;
import static com.webmbt.mscript.parse.MScriptLexer.RBRACK;
import static java.lang.String.valueOf;

/**
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Mar 06, 2015
 */
public class MScriptEngine {

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

        return new MScriptEvalVisitor(systemFunctions, availablePlugins).visit(mScriptParseTree);
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
                throw new IllegalArgumentException("the list to accumulate MScript errors cannot be null");
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

        protected Functions functions;

        protected MbtScriptExecutor systemFunctions;

        protected List<PluginAncestor> availablePlugins;

        public MScriptEvalVisitor(MbtScriptExecutor systemFunctions, List<PluginAncestor> availablePlugins) {
            this(null, systemFunctions, availablePlugins);
        }

        public MScriptEvalVisitor(Functions functions, MbtScriptExecutor systemFunctions,
                                  List<PluginAncestor> availablePlugins) {
            this.functions = functions == null ? Functions.DEFAULT_INSTANCE : functions;
            this.systemFunctions = systemFunctions;
            this.availablePlugins = availablePlugins;
        }

        @Override
        public String visitCond(@NotNull MScriptParser.CondContext ctx) {
            if (ctx.op == null) { // no operator specified, simply evaluate and return the expression
                return visitExpr(ctx.expr(0));
            }

            System.out.println(ctx.op);
            return "";
        }

        @Override
        public String visitAtom(@NotNull MScriptParser.AtomContext ctx) {
            TerminalNode terminal = ctx.FLOAT();
            if (terminal != null) {
                return valueOf(terminal.getText());
            }

            terminal = ctx.INTEGER();
            if (terminal != null) {
                return valueOf(terminal.getText());
            }

            terminal = ctx.BOOLEAN();
            if (terminal != null) {
                return valueOf(terminal.getText());
            }

            terminal = ctx.ID();
            if (terminal != null) {
                // If one hasn't provided a system functions implementation,
                // we try to be forgiving and simply return 'null'
                return systemFunctions == null ? "null" : systemFunctions.getVar(terminal.getText());
            }

            return visit(ctx.getChild(0));
        }

        @Override
        public String visitString(@NotNull MScriptParser.StringContext ctx) {
            StringBuilder acc = new StringBuilder();

            int partsCount = ctx.getChildCount() - 1;
            for (int i = 1; i < partsCount; i++) {
                ParseTree part = ctx.getChild(i);

                if (part instanceof TerminalNode) {
                    Token token = ((TerminalNode) part).getSymbol();
                    int tokenType = token.getType();
                    if (tokenType != IN_STR_LBRACK && tokenType != RBRACK) {
                        acc.append(token.getText());
                    }
                } else {
                    acc.append(visit(part));
                }
            }

            return acc.toString();
        }

        @Override
        public String visitFncall(@NotNull MScriptParser.FncallContext ctx) {
            String pluginName = ctx.plugin == null ? null : ctx.plugin.getText();
            String functionName = ctx.function.getText();

            // If the lookup service / cache is reused between the parser and the visitor,
            // the function would have already been validated so the lookup is much faster:
            Lookup lookup = functions.lookup(pluginName, functionName, ctx.argc, systemFunctions, availablePlugins);
            if (lookup.result != FOUND) { // should not happen!
                throw new RuntimeException(
                    "Cannot invoke function $" + (pluginName == null ? "" : pluginName + ".") + functionName);
            }

            String[] args = ctx.argc == 0 ? null : new String[ctx.argc];
            List<? extends MScriptParser.ExprContext> arguments = ctx.expr();
            if (ctx.argc > 0) {
                for (int i = 0; i < ctx.argc; i++) {
                    args[i] = visit(arguments.get(i));
                }
            }

            return lookup.function.call(args);
        }
    }

    private Functions functions = new Functions();

    public MScriptEngine clearFunctionCache() {
        functions.clearCache();
        return this;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("RESULT: [" + new MScriptEngine()
            .executeMScript("'1$getVar('boo')['a['b']']'", new MbtScriptExecutor(), new ArrayList<PluginAncestor>()) +
                           "]");
    }
}
