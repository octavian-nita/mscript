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

protected void check(boolean condition, String errorMessage) {
    if (!condition) {
        throw new MScriptRecognitionException(errorMessage, this);
    }
}

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

block : ( CM | NL | SEMI )* stmt ( CM* ( NL | SEMI ) CM* stmt? )* ;

stmt
  : assign
  | fncall
  | ifStmt
  | whileStmt
  | breakStmt
  | continueStmt
  ;

assign : ID ( CM | NL )* ASSIGN ( CM | NL )* expr ;

fncall
locals [int argc=0, String plugin=null, String function=null] // match and store plugin and function names
  : SIGIL // followed by...

    ( ID {$plugin = $ID.text;} DOT )? ID {$function = $ID.text;} ( CM | NL )*

    // match and count arguments to validate the call
    LPAREN ( CM | NL )* ( expr {$argc++;} ( ( CM | NL )* COMMA ( CM | NL )* expr {$argc++;} )* )? ( CM | NL )* RPAREN {

// After matching the whole function call, validate function name and arguments:
switch (Function.check($plugin, $function, $argc)) {
case NO_SUCH_FUNCTION:
    throw new MScriptRecognitionException("$" + ($plugin != null ? $plugin + "." : "") + $function +
                                          ": no such function defined", this, $SIGIL);
case WRONG_NUM_OF_ARGS:
    throw new MScriptRecognitionException("$" + ($plugin != null ? $plugin + "." : "") + $function +
                                          ": function cannot be called with " + $argc + " arguments", this, $SIGIL);
}

    } ;

ifStmt
  : IF ( CM | NL )* LPAREN ( CM | NL )* cond ( CM | NL )* RPAREN ( CM | NL )*

    ( LBRACE ( block? | ( CM | NL | SEMI )* ) RBRACE | stmt ( ( CM | NL )* SEMI ( CM | NL )* )? )

    // optional ELSE branch
    ( ( CM | NL )* ELSE ( CM | NL )*

      ( LBRACE ( block? | ( CM | NL | SEMI )* ) RBRACE | stmt ( ( CM | NL )* SEMI ( CM | NL )* )? ) )? ;

cond
  : expr ( CM | NL )* ( EQ | NE | LE | LT | GE | GT ) ( CM | NL )* expr
  | expr // in order to allow statements like while (v) { ... } or if ('true') { ... }
  ;

whileStmt
  : WHILE ( CM | NL )*

    LPAREN ( CM | NL )* cond ( CM | NL )* ( ( CM | NL )* PIPE ( CM | NL )* whileOpts ( CM | NL )* )? RPAREN ( CM | NL )*
    {++loopDepth;}

    ( LBRACE ( block? | ( CM | NL | SEMI )* ) RBRACE | stmt ( ( CM | NL )* SEMI ( CM | NL )* )? )
    {if (loopDepth > 0) { --loopDepth; }} ;

whileOpts
  : ID
  ;

breakStmt : BREAK {check(loopDepth > 0, "break cannot be used outside of a loop");} ;

continueStmt : CONTINUE {check(loopDepth > 0, "continue cannot be used outside of a loop");} ;

expr
  : expr ( CM | NL )* ( MUL | DIV | MOD ) ( CM | NL )* expr
  | expr ( CM | NL )* ( ADD | SUB ) ( CM | NL )* expr
  | ( ADD | SUB )? ( CM | NL )* LPAREN ( CM | NL )* expr ( CM | NL )* RPAREN // parenthesized expression
  | ( ADD | SUB )? ( CM | NL )* fncall
  | ( ADD | SUB )? ( CM | NL )* string
  | ( ADD | SUB )? ( CM | NL )* BOOLEAN
  | ( ADD | SUB )? ( CM | NL )* NUMBER
  | ( ADD | SUB )? ( CM | NL )* ID
  ;

string : QUOTE ( STR_CHARS | fncall | IN_STR_LBRACK expr RBRACK )* IN_STR_QUOTE ;
