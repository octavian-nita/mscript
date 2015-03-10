/**
 * MScript parser definition.
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Sep 27, 2014
 */
parser grammar MScriptParser;

options { tokenVocab=MScriptLexer; }

@header {
import com.webmbt.plugin.MbtScriptExecutor;
import com.webmbt.plugin.PluginAncestor;

import com.webmbt.mscript.Functions;
import com.webmbt.mscript.Functions.LookupResult;
import com.webmbt.mscript.parse.MScriptRecognitionException;

import java.util.List;
}

@members {

protected static final class WhileOptions {
    public boolean hasIndex;
    public boolean hasLabel;
    public boolean hasMaxLoopNum;
}

protected final void check(boolean condition, String errorMessage) {
    if (!condition) {
        throw new MScriptRecognitionException(errorMessage, this);
    }
}

/**
 * The current level of loop nesting (0 for top level statements). Used, for example, to determine at parse time whether
 * a matched break or continue statement can indeed be accepted.
 */
protected int loopDepth;

protected Functions functions;

public MScriptParser(TokenStream input, Functions functions) {
		this(input);
		this.functions = functions;
}

public MScriptParser setFunctions(Functions functions) {
		this.functions = functions;
		return this;
}

}

//
// ABOUT REPEATED COMMENT AND NEWLINE TOKENS:
//
// Currently, the requirement is to keep comments in the abstract syntax tree (AST). Therefore, they cannot be skipped
// in the lexer but kept as tokens and repeatedly specified in all parser rules where comments may appear, between any
// two consecutive tokens. We also allow newlines between many consecutive tokens.
// (see http://stackoverflow.com/questions/12485132/catching-and-keeping-all-comments-with-antlr).
//
// Other points to remember: ANTLR v4+ automatically builds an AST, does not include tokens on channels other that the
// default one (unless one extends CommonTokenStream) and does not really give the developer much choice to manually
// (re)organize the AST (apart from how one writes the grammar).
//

script : ( pad | SEMI )* stats? EOF ;

stats  : ( pad | SEMI )* stat ( COMM* ( NL | SEMI ) COMM* stat? )* ;

stat   : assign | fncall | ifStat | whileStat | breakStat | continueStat ;

assign : ID pad* ASSIGN pad* expr ;

fncall [MbtScriptExecutor systemFunctions, List<PluginAncestor> availablePlugins]
locals [String plugin, String function, int argc] // match and store plugin and function names
  : SIGIL // followed by...

    ( ID {$plugin = $ID.text;} DOT )? ID {$function = $ID.text;} pad*

    // match and count arguments to validate the call
    LPAREN pad* ( expr {$argc++;} ( pad* COMMA pad* expr {$argc++;} )* )? pad* RPAREN {

// After matching the whole function call, validate function name and arguments:

if (functions == null) {
    throw new MScriptRecognitionException("$" + ($plugin != null ? $plugin + "." : "") + $function +
                                          ": no function function specified for the parser", this, $SIGIL);
}
/*
switch (library.check($plugin, $function, $argc)) {
case NO_SUCH_PLUGIN:
    throw new MScriptRecognitionException("$" + ($plugin != null ? $plugin + "." : "") + $function +
                                          ": no such plugin defined", this, $SIGIL);
case NO_SUCH_FUNCTION:
    throw new MScriptRecognitionException("$" + ($plugin != null ? $plugin + "." : "") + $function +
                                          ": no such function defined", this, $SIGIL);
case TOO_FEW_ARGUMENTS:
    throw new MScriptRecognitionException("$" + ($plugin != null ? $plugin + "." : "") + $function +
                                          ": too few arguments (" + $argc + ") provided", this, $SIGIL);
case TOO_MANY_ARGUMENTS:
    throw new MScriptRecognitionException("$" + ($plugin != null ? $plugin + "." : "") + $function +
                                          ": too many arguments (" + $argc + ") provided", this, $SIGIL);
}
*/
    } ;

ifStat
  : IF pad* LPAREN pad* cond pad* RPAREN pad* ( LBRACE ( stats? | ( pad | SEMI )* ) RBRACE | stat )

    ( pad* ELSE pad* ( LBRACE ( stats? | ( pad | SEMI )* ) RBRACE | stat ) )? ; // optional ELSE branch

cond
  : expr pad* ( EQ | NE | LE | LT | GE | GT ) pad* expr
  | expr // in order to allow statements like while (v) { ... } or if ('true') { ... }
  ;

whileStat
  : WHILE pad*

    LPAREN pad* cond pad* ( pad* PIPE pad* whileOpts pad* )? RPAREN pad* {++loopDepth;}

    ( LBRACE ( stats? | ( pad | SEMI )* ) RBRACE | stat ) {if (loopDepth > 0) { --loopDepth; }} ;

//
// Trying to be as specfic as possible when describing the named while options in order to catch as
// many errors as possible and to take advantage of the ANTLR's built-in error handling mechanism.
//

whileOpts
locals [WhileOptions options=new WhileOptions()]
  : namedWhileOpts[$options]
  | ID {$options.hasIndex = true;}

  | ID {$options.hasIndex = true;} pad* SEMI pad*
    namedWhileOpts[$options]

  | ID {$options.hasIndex = true;} pad* SEMI pad*
    INTEGER {$options.hasMaxLoopNum = true;}

  | ID {$options.hasIndex = true;} pad* SEMI pad*
    INTEGER {$options.hasMaxLoopNum = true;} pad* SEMI pad*
    namedWhileOpts[$options]

  | ID {$options.hasIndex = true;} pad* SEMI pad*
    ID {$options.hasLabel = true;}

  | ID {$options.hasIndex = true;} pad* SEMI pad*
    ID {$options.hasLabel = true;} pad* SEMI pad*
    namedWhileOpts[$options]

  | ID {$options.hasIndex = true;} pad* SEMI pad*
    INTEGER {$options.hasMaxLoopNum = true;} pad* SEMI pad*
    ID {$options.hasLabel = true;}

  | ID {$options.hasIndex = true;} pad* SEMI pad*
    INTEGER {$options.hasMaxLoopNum = true;} pad* SEMI pad*
    ID {$options.hasLabel = true;} pad* SEMI pad*
    namedWhileOpts[$options]
  ;

namedWhileOpts[WhileOptions options]
  : namedWhileOpt[$options] ( pad* SEMI pad* namedWhileOpt[$options] )? ( pad* SEMI pad* namedWhileOpt[$options] )? ;

namedWhileOpt[WhileOptions options]
  : optionName=ID pad* ASSIGN pad* (optionVal=ID | optionIntVal=INTEGER) {

if (options != null) {
    String optionName = $optionName.getText();
    switch (optionName) {
    case "index":
        check(!options.hasIndex, "'index' already defined for the current loop");
        check($optionVal != null, "'index' can only be assigned an identifier");
        options.hasIndex = true;
        break;
    case "maxLoopNum":
        check(!options.hasMaxLoopNum, "'maxLoopNum' already defined for the current loop");
        check($optionIntVal != null, "'maxLoopNum' can only be assigned a positive integer");
        options.hasMaxLoopNum = true;
        break;
    case "label":
        check(!options.hasLabel, "'label' already defined for the current loop");
        check($optionVal != null, "'label' can only be assigned an identifier");
        options.hasLabel = true;
        break;
    default:
        throw new MScriptRecognitionException("unexpected loop option '" + optionName + "'", this);
    }
}

};

breakStat : BREAK pad* ID? {check(loopDepth > 0, "break cannot be used outside of a loop");} ;

continueStat : CONTINUE pad* ID? {check(loopDepth > 0, "continue cannot be used outside of a loop");} ;

expr
  : expr pad* ( MUL | DIV | MOD ) pad* expr
  | expr pad* ( ADD | SUB ) pad* expr
  | ( ADD | SUB )? pad* LPAREN pad* expr pad* RPAREN // parenthesized expression
  | ( ADD | SUB )? pad* ( fncall | string | FLOAT | INTEGER | BOOLEAN | ID )
  ;

string : QUOTE ( IN_STR_CHARS | fncall | IN_STR_LBRACK expr RBRACK )* IN_STR_QUOTE ;

pad : COMM | NL ; // comments and newlines can appear between (many) consecutive tokens
