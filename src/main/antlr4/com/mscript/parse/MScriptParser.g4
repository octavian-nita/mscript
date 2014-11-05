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
import com.mscript.parse.FunctionRecognitionException;
}

@members {

/**
 * The current level of loop nesting (0 for top level statements). Used, for example, to determine at parse time whether
 * a break or continue statement can indeed be accepted.
 */
protected int loopDepth;

}

script : block? EOF ;

block : ( SC | MC | NL | SEMI )* stmt ( ( SC | MC )* ( NL | SEMI ) ( SC | MC )* stmt? )* ;

stmt
  : assign
  | fncall
  | ifStmt
  ;

assign : ID ( SC | MC | NL )* ASSIGN ( SC | MC | NL )* expr ;

fncall
locals [int argsCount=0, String pluginName=null, String functionName=null]
  : SIGIL // rule hasn't ended; followed by...

    ( ID {$pluginName = $ID.text;} DOT )? ID {$functionName = $ID.text;} // match and store plugin and function names

    ( SC | MC | NL )* // optional comments and new lines; quite a free-form language...

    LPAREN ( SC | MC | NL )*

    // match and count arguments to validate the call
    ( expr {$argsCount++;} ( ( SC | MC | NL )* COMMA ( SC | MC | NL )* expr {$argsCount++;} )* )?

    ( SC | MC | NL )* RPAREN {

// After matching the whole function call, validate function name and arguments:
switch (Function.check($pluginName, $functionName, $argsCount)) {
case NO_SUCH_FUNCTION:
    throw new FunctionRecognitionException("no such function: $" + ($pluginName != null ? $pluginName + "." : "") +
                                           $functionName + " defined", this, $SIGIL);
case WRONG_NUM_OF_ARGS:
    throw new FunctionRecognitionException("function: $" + ($pluginName != null ? $pluginName + "." : "") +
                                           $functionName + " cannot be called with " + $argsCount + " arguments", this,
                                           $SIGIL);
}

    } ;

ifStmt
  : IF ( SC | MC | NL )* LPAREN ( SC | MC | NL )* cond ( SC | MC | NL )* RPAREN ( SC | MC | NL )*

    ( LBRACE ( block? | ( SC | MC | NL | SEMI )* ) RBRACE
    | stmt ( ( SC | MC | NL )* SEMI ( SC | MC | NL )* )? )

    // optional ELSE branch
    ( ( SC | MC | NL )* ELSE ( SC | MC | NL )*
      ( LBRACE ( block? | ( SC | MC | NL | SEMI )* ) RBRACE
      | stmt ( ( SC | MC | NL )* SEMI ( SC | MC | NL )* )? ) )? ;

cond
  : expr ( SC | MC | NL )* ( EQ | NE | LE | LT | GE | GT ) ( SC | MC | NL )* expr
  | expr // in order to allow statements like while (v) { ... } or if ('true') { ... }
  ;

expr
  : expr ( SC | MC | NL )* ( MUL | DIV | MOD ) ( SC | MC | NL )* expr
  | expr ( SC | MC | NL )* ( ADD | SUB ) ( SC | MC | NL )* expr
  | ( ADD | SUB )? ( SC | MC | NL )* LPAREN ( SC | MC | NL )* expr ( SC | MC | NL )* RPAREN // parenthesized expression
  | ( ADD | SUB )? ( SC | MC | NL )* fncall
  | ( ADD | SUB )? ( SC | MC | NL )* string
  | ( ADD | SUB )? ( SC | MC | NL )* BOOLEAN
  | ( ADD | SUB )? ( SC | MC | NL )* NUMBER
  | ( ADD | SUB )? ( SC | MC | NL )* ID
  ;

string : QUOTE ( STR_CHARS | fncall | IN_STR_LBRACK expr RBRACK )* IN_STR_QUOTE ;
