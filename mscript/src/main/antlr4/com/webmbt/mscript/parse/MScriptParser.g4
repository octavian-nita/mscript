/**
 * <p>
 * MScript parser definition.
 * </p>
 * <p>
 * The current design of the parser requires creating a new instance for each parsing operation. Creating such instances
 * is rather cheap, see <a href="http://groups.google.com/d/msg/antlr-discussion/B2TaUFm29jE/1UmKQKHhFEcJ">here</a>.
 * </p>
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.1, Mar 13, 2015
 */
parser grammar MScriptParser;

options { tokenVocab=MScriptLexer; }

@header {
import com.webmbt.mscript.Functions;
import com.webmbt.mscript.Functions.Lookup;

import com.webmbt.plugin.MbtScriptExecutor;
import com.webmbt.plugin.PluginAncestor;

import java.util.List;

import static com.webmbt.mscript.Functions.Lookup.Result.E_PLUGIN_NOT_FOUND;
import static com.webmbt.mscript.Functions.Lookup.Result.FOUND;
}

@members {

protected static final class WhileOptions {
    public boolean hasIndex;
    public boolean hasLabel;
    public boolean hasMaxLoopNum;
}

protected final void check(boolean condition, String errorCode, Object ...errorArguments) {
    if (!condition) {
        throw new MScriptRecognitionException(this, errorCode, errorArguments);
    }
}

/**
 * The current level of loop nesting (0 for top level statements). Used to determine (at
 * parse time) whether a matched break or continue statement can indeed be accepted.
 */
protected int loopDepth;

/**
 * The (caching) lookup service for MScript {@link Function functions}. If not explicitly provided at
 * parser instance creation time, a {@link Functions#DEFAULT_INSTANCE default instance} will be used.
 */
protected Functions functions;

protected MbtScriptExecutor systemFunctions;

protected List<PluginAncestor> availablePlugins;

/**
 * Equivalent to <code>this(input, null, systemFunctions, availablePlugins)</code>.
 */
public MScriptParser(TokenStream input,
                     MbtScriptExecutor systemFunctions, List<PluginAncestor> availablePlugins) {
    this(input, null, systemFunctions, availablePlugins);
}

/**
 * The current design of the parser requires creating a new instance for each parsing operation. Creating such instances
 * is rather cheap, see <a href="http://groups.google.com/d/msg/antlr-discussion/B2TaUFm29jE/1UmKQKHhFEcJ">here</a>.
 */
public MScriptParser(TokenStream input, Functions functions,
                     MbtScriptExecutor systemFunctions, List<PluginAncestor> availablePlugins) {
		this(input);
		this.functions = functions == null ? Functions.DEFAULT_INSTANCE : functions;
		this.systemFunctions = systemFunctions;
		this.availablePlugins = availablePlugins;
}

}

//
// ABOUT REPEATED COMMENT AND NEWLINE TOKENS:
//
// Currently, the requirement is to keep comments in the abstract syntax tree (AST). Therefore, they cannot be skipped
// in the lexer but kept as tokens and repeatedly specified in all parser rules where comments may appear, between any
// two other consecutive tokens. We also allow newlines between many consecutive tokens.
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

fncall
locals [Token plugin, Token function, int argc] // match and store plugin and function names and argument count
  : SIGIL ( ID {$plugin = $ID;} DOT )? ID {$function = $ID;} pad* // followed by...

    // match and count arguments to validate the call
    LPAREN pad* ( expr {$argc++;} ( pad* COMMA pad* expr {$argc++;} )* )? pad* RPAREN {

        // After matching the whole function call, validate function name and arguments:
        Lookup lookup = functions.lookup($plugin != null ? $plugin.getText() : null, $function.getText(),
                                         $argc, systemFunctions, availablePlugins);
        if (lookup.result != FOUND) {
            throw new MScriptRecognitionException(this,
                                                  lookup.result == E_PLUGIN_NOT_FOUND ? $plugin : $function,
                                                  lookup.result.toString());
        }

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
          switch ($optionName.getText()) {
          case "index":
              check(!options.hasIndex, "E_PARSE_LOOP_INDEX_SPECKED");
              check($optionVal != null, "E_PARSE_LOOP_INDEX_INVALID");
              options.hasIndex = true;
              break;
          case "maxLoopNum":
              check(!options.hasMaxLoopNum, "E_PARSE_LOOP_MAX_NUM_SPECKED");
              check($optionIntVal != null, "E_PARSE_LOOP_MAX_NUM_INVALID");
              options.hasMaxLoopNum = true;
              break;
          case "label":
              check(!options.hasLabel, "E_PARSE_LOOP_LABEL_SPECKED");
              check($optionVal != null, "E_PARSE_LOOP_LABEL_INVALID");
              options.hasLabel = true;
              break;
          default:
              throw new MScriptRecognitionException(this, "E_PARSE_LOOP_UNEXPECTED_OPTION");
          }
      }

};

breakStat : BREAK pad* ID? {check(loopDepth > 0, "E_PARSE_BREAK_NOT_ALLOWED");} ;

continueStat : CONTINUE pad* ID? {check(loopDepth > 0, "E_PARSE_CONTINUE_NOT_ALLOWED");} ;

expr
  : expr pad* ( MUL | DIV | MOD ) pad* expr
  | expr pad* ( ADD | SUB ) pad* expr
  | ( ADD | SUB )? pad* LPAREN pad* expr pad* RPAREN // parenthesized expression
  | ( ADD | SUB )? pad* ( fncall | string | FLOAT | INTEGER | BOOLEAN | ID )
  ;

string : QUOTE ( IN_STR_CHARS | fncall | IN_STR_LBRACK expr RBRACK )* IN_STR_QUOTE ;

pad : COMM | NL ; // comments and newlines can appear between (many) consecutive tokens
