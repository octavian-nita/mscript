/**
 * MScript parser definition.
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Sep 27, 2014
 */
parser grammar MScriptParser;

options { tokenVocab=MScriptLexer; }

@header {
import com.mscript.Function;
import com.mscript.Function.CheckResult;
import com.mscript.parse.MScriptRecognitionException;
}

@members {

/**
 * The current level of loop nesting (0 for top level statements). Used, for example, to determine at parse time whether
 * a break or continue statement can indeed be accepted.
 */
protected int loopDepth;

}

// ABOUT REPEATED COMMENT AND NEWLINE TOKENS:
//
// Currently, the requirement is to keep comments (both single and multi-line) in the abstract syntax tree (AST).
// Therefore, they cannot be skipped in lexer but kept as tokens that must be repeated in all parser rules where
// comments may appear (normally between any two consecutive tokens). We also choose to allow newlines in-between
// tokens of a parser rule.
// (see http://stackoverflow.com/questions/12485132/catching-and-keeping-all-comments-with-antlr)
//
// Also, ANTLR v4+ automatically builds the AST, does not include tokens on other channels that the default one (unless
// one extends CommonTokenStream) and does not really give the developer much choice in manually (re)organizing the AST
// (apart from how one writes the grammar). One solution would be to traverse the AST created by parsing and manually
// build a new one using the ANTLR API, in an additional parsing phase. This would also allow one to clean up / remove
// useless tokens like surrounding quotes for strings, operator nodes, etc.

script : block? EOF ;

block : ( COMM | NL | SEMI )* stmt ( COMM* ( NL | SEMI ) COMM* stmt? )* ;

stmt
  : assign
  | fncall
  | ifStmt
  | whileStmt
  | breakStmt
  | continueStmt
  ;

assign : ID ( COMM | NL )* ASSIGN ( COMM | NL )* expr ;

fncall
locals [int argsCount=0, String pluginName=null, String functionName=null]
  : SIGIL // rule hasn't ended; followed by...

    ( ID {$pluginName = $ID.text;} DOT )? ID {$functionName = $ID.text;} // match and store plugin and function names

    ( COMM | NL )* // optional comments and new lines; quite a free-form language...

    LPAREN ( COMM | NL )*

    // match and count arguments to validate the call
    ( expr {$argsCount++;} ( ( COMM | NL )* COMMA ( COMM | NL )* expr {$argsCount++;} )* )?

                           ( COMM | NL )* RPAREN {

// After matching the whole function call, validate function name and arguments:
switch (Function.check($pluginName, $functionName, $argsCount)) {
case NO_SUCH_FUNCTION:
    throw new MScriptRecognitionException("$" + ($pluginName != null ? $pluginName + "." : "") + $functionName +
                                           ": no such function defined", this, $SIGIL);
case WRONG_NUM_OF_ARGS:
    throw new MScriptRecognitionException("$" + ($pluginName != null ? $pluginName + "." : "") + $functionName +
                                           ": function cannot be called with " + $argsCount + " arguments", this, $SIGIL);
}

    } ;

ifStmt
  : IF ( COMM | NL )* LPAREN ( COMM | NL )* cond ( COMM | NL )* RPAREN ( COMM | NL )*

    ( LBRACE ( block? | ( COMM | NL | SEMI )* ) RBRACE
    | stmt ( ( COMM | NL )* SEMI ( COMM | NL )* )? )

    // optional ELSE branch
    ( ( COMM | NL )* ELSE ( COMM | NL )*

      ( LBRACE ( block? | ( COMM | NL | SEMI )* ) RBRACE
      | stmt ( ( COMM | NL )* SEMI ( COMM | NL )* )? ) )? ;

    cond
  : expr ( COMM | NL )* ( EQ | NE | LE | LT | GE | GT ) ( COMM | NL )* expr
  | expr // in order to allow statements like while (v) { ... } or if ('true') { ... }
  ;

whileStmt
  : WHILE ( COMM | NL )* LPAREN ( COMM | NL )* cond ( COMM | NL )* RPAREN ( COMM | NL )* {++loopDepth;}

  ( LBRACE ( block? | ( COMM | NL | SEMI )* ) RBRACE
  | stmt ( ( COMM | NL )* SEMI ( COMM | NL )* )? ) {if (loopDepth > 0) { --loopDepth; }} ;

breakStmt
  : BREAK {

if (loopDepth <= 0) {
    throw new MScriptRecognitionException("break cannot be used outside of a loop", this);
}

  } ;

continueStmt
  : CONTINUE {

if (loopDepth <= 0) {
    throw new MScriptRecognitionException("continue cannot be used outside of a loop", this);
}

  } ;

expr
  : expr ( COMM | NL )* ( MUL | DIV | MOD ) ( COMM | NL )* expr
  | expr ( COMM | NL )* ( ADD | SUB ) ( COMM | NL )* expr
  | ( ADD | SUB )? ( COMM | NL )* LPAREN ( COMM | NL )* expr ( COMM | NL )* RPAREN // parenthesized expression
  | ( ADD | SUB )? ( COMM | NL )* fncall
  | ( ADD | SUB )? ( COMM | NL )* string
  | ( ADD | SUB )? ( COMM | NL )* BOOLEAN
  | ( ADD | SUB )? ( COMM | NL )* NUMBER
  | ( ADD | SUB )? ( COMM | NL )* ID
  ;

string : QUOTE ( STR_CHARS | fncall | IN_STR_LBRACK expr RBRACK )* IN_STR_QUOTE ;
