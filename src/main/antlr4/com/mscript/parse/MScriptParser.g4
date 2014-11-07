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

//
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
//

script : block? EOF ;

block  : ( pad | SEMI )* stmt ( COMM* ( NL | SEMI ) COMM* stmt? )* ;

stmt   : assign | fncall | ifStmt | whileStmt | breakStmt | continueStmt ;

assign : ID pad* ASSIGN pad* expr ;

fncall
locals [int argc=0, String plugin=null, String function=null] // match and store plugin and function names
  : SIGIL // followed by...

    ( ID {$plugin = $ID.text;} DOT )? ID {$function = $ID.text;} pad*

    // match and count arguments to validate the call
    LPAREN pad* ( expr {$argc++;} ( pad* COMMA pad* expr {$argc++;} )* )? pad* RPAREN {

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
  : IF pad* LPAREN pad* cond pad* RPAREN pad* ( LBRACE ( block? | ( pad | SEMI )* ) RBRACE | stmt )

    ( pad* ELSE pad* ( LBRACE ( block? | ( pad | SEMI )* ) RBRACE | stmt ) )? ; // optional ELSE branch

cond
  : expr pad* ( EQ | NE | LE | LT | GE | GT ) pad* expr
  | expr // in order to allow statements like while (v) { ... } or if ('true') { ... }
  ;

whileStmt
  : WHILE pad*

    LPAREN pad* cond pad* ( pad* PIPE pad* whileOpts pad* )? RPAREN pad* {++loopDepth;}

    ( LBRACE ( block? | ( pad | SEMI )* ) RBRACE | stmt ) {if (loopDepth > 0) { --loopDepth; }} ;

whileOpts
  : namedWhileOpts
  | ID ( pad* SEMI pad* ( INTEGER | namedWhileOpts )
  )?
  ;

namedWhileOpts : namedWhileOpt ( pad* SEMI pad* namedWhileOpt )? ( pad* SEMI pad* namedWhileOpt )? ; // simulate {,3}

namedWhileOpt : ID pad* ASSIGN pad* (ID | INTEGER) {
// TODO: check correct 'pseudo-tokens' and values
};

breakStmt : BREAK pad* ID? {check(loopDepth > 0, "break cannot be used outside of a loop");} ;

continueStmt : CONTINUE pad* ID? {check(loopDepth > 0, "continue cannot be used outside of a loop");} ;

expr
  : expr pad* ( MUL | DIV | MOD ) pad* expr
  | expr pad* ( ADD | SUB ) pad* expr
  | ( ADD | SUB )? pad* LPAREN pad* expr pad* RPAREN // parenthesized expression
  | ( ADD | SUB )? pad* ( fncall | string | FLOAT | INTEGER | BOOLEAN | ID )
  ;

string : QUOTE ( STR_CHARS | fncall | IN_STR_LBRACK expr RBRACK )* IN_STR_QUOTE ;

pad : COMM | NL ; // comments and newlines can appear between (many) consecutive tokens
