package com.webmbt.mscript;

import com.webmbt.mscript.parse.MScriptParser;
import com.webmbt.mscript.parse.MScriptParserBaseVisitor;
import com.webmbt.plugin.MbtScriptExecutor;
import com.webmbt.plugin.PluginAncestor;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.webmbt.mscript.Functions.Lookup.Result.FOUND;
import static com.webmbt.mscript.Types.asNumber;
import static com.webmbt.mscript.parse.MScriptLexer.*;
import static java.lang.String.valueOf;
import static java.util.regex.Pattern.compile;

class MScriptEvalVisitor extends MScriptParserBaseVisitor<String> {

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
        if (ctx.condOp == null) { // no operator specified, simply evaluate and return the expression
            return visit(ctx.expr(0));
        }

        int opType = ctx.condOp.getType();
        String val1 = visit(ctx.expr(0));
        String val2 = visit(ctx.expr(1));

        switch (opType) {
        case EQ:
            try {
                return valueOf(asNumber(val1) == asNumber(val2));
            } catch (NumberFormatException nfe) {
                return valueOf(val1.equals(val2));
            }
        case NE:
            try {
                return valueOf(asNumber(val1) != asNumber(val2));
            } catch (NumberFormatException nfe) {
                return valueOf(!val1.equals(val2));
            }
        case LE:
            try {
                return valueOf(asNumber(val1) <= asNumber(val2));
            } catch (NumberFormatException nfe) {
                return valueOf(val1.compareTo(val2) <= 0);
            }
        case LT:
            try {
                return valueOf(asNumber(val1) < asNumber(val2));
            } catch (NumberFormatException nfe) {
                return valueOf(val1.compareTo(val2) < 0);
            }
        case GE:
            try {
                return valueOf(asNumber(val1) >= asNumber(val2));
            } catch (NumberFormatException nfe) {
                return valueOf(val1.compareTo(val2) >= 0);
            }
        case GT:
            try {
                return valueOf(asNumber(val1) > asNumber(val2));
            } catch (NumberFormatException nfe) {
                return valueOf(val1.compareTo(val2) > 0);
            }
        default:
            throw new RuntimeException("Unsupported conditional operator: " + ctx.condOp.getText());
        }
    }

    @Override
    public String visitMulDivMod(@NotNull MScriptParser.MulDivModContext ctx) {
        int opType = ctx.binOp.getType();
        String val1 = visit(ctx.expr(0));
        String val2 = visit(ctx.expr(1));

        switch (opType) {
        case MUL:
            return systemFunctions.mul(val1, val2);
        case DIV:
            return systemFunctions.div(val1, val2);
        case MOD:
            return systemFunctions.mod(val1, val2);
        default:
            throw new RuntimeException("Unsupported arithmetic operator: " + ctx.binOp.getText());
        }
    }

    @Override
    public String visitAddSub(@NotNull MScriptParser.AddSubContext ctx) {
        int opType = ctx.binOp.getType();
        String val1 = visit(ctx.expr(0));
        String val2 = visit(ctx.expr(1));

        switch (opType) {
        case ADD:
            return systemFunctions.add(val1, val2);
        case SUB:
            return systemFunctions.sub(val1, val2);
        default:
            throw new RuntimeException("Unsupported arithmetic operator: " + ctx.binOp.getText());
        }
    }

    @Override
    public String visitParens(@NotNull MScriptParser.ParensContext ctx) {
        String val = visit(ctx.expr());
        return ctx.unaryOp != null && ctx.unaryOp.getType() == SUB ? valueOf(-asNumber(val)) : val;
    }

    @Override
    public String visitExAtom(@NotNull MScriptParser.ExAtomContext ctx) {
        String val = visit(ctx.atom());
        return ctx.unaryOp != null && ctx.unaryOp.getType() == SUB ? valueOf(-asNumber(val)) : val;
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
    public String visitFncall(@NotNull MScriptParser.FncallContext ctx) {
        String pnName = ctx.plugin == null ? null : ctx.plugin.getText();
        String fnName = ctx.function.getText();

        // If the lookup service / cache is reused between the parser and the visitor,
        // the function would have already been validated so this new lookup is much faster:
        Functions.Lookup lookup = functions.lookup(pnName, fnName, ctx.argc, systemFunctions, availablePlugins);
        if (lookup.result != FOUND) { // should not happen but we're trying to stay defensive here...
            throw new Function.CallException(null, "Cannot invoke function $" +
                                                   (pnName == null ? "" : pnName + ".") + fnName);
        }

        String[] args = null;
        if (ctx.argc > 0) {
            args = new String[ctx.argc];

            List<? extends MScriptParser.ExprContext> arguments = ctx.expr();
            for (int i = 0; i < ctx.argc; i++) {
                args[i] = visit(arguments.get(i));
            }
        }

        return lookup.function.call(args);
    }

    @Override
    public String visitString(@NotNull MScriptParser.StringContext ctx) {
        StringBuilder acc = new StringBuilder();

        int partsCount = ctx.getChildCount() - 1; // ignore opening and closing quotation marks
        for (int i = 1; i < partsCount; i++) {
            ParseTree part = ctx.getChild(i);

            if (part instanceof TerminalNode) { // translate MScript string part to Java
                Token token = ((TerminalNode) part).getSymbol();
                int tokenType = token.getType();
                if (tokenType != IN_STR_LBRACK && tokenType != RBRACK) {
                    String tx = token.getText();

                    // Could we do better / faster here, like in only one string traversal?
                    for (Map.Entry<Pattern, String> esc : ESC.entrySet()) {
                        tx = esc.getKey().matcher(tx).replaceAll(esc.getValue());
                    }

                    acc.append(tx);
                }
            } else {
                acc.append(visit(part));
            }
        }

        return acc.toString();
    }

    protected static final Map<Pattern, String> ESC = new LinkedHashMap<>();

    // Avoid re-compiling the escape character patterns for each string translation from MScript to Java:
    static {
        ESC.put(compile("\\\\\\\\"), "\\\\");
        ESC.put(compile("\\\\'"), "'");
        ESC.put(compile("\\\\\\$"), "\\$");
        ESC.put(compile("\\\\\\["), "[");
        ESC.put(compile("\\\\\\]"), "]");
        ESC.put(compile("\\\\n"), "\n");
        ESC.put(compile("\\\\r"), "\r");
        ESC.put(compile("\\\\t"), "\t");
    }
}
