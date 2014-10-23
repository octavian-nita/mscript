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

script : block? EOF ;

block : SEPS? stat ( SEPS stat? )* ;

stat
  : assign
  | fncall
  | ifStat
  ;

assign : ID ASSIGN expr ;

fncall
locals [int argsCount=0, String pluginName=null, String functionName=null]
  : SIGIL // rule hasn't ended; followed by...

    // As we match the (eventual) plugin and function names, we store references to them:
    ( ID {$pluginName = $ID.text;} DOT )? ID {$functionName = $ID.text;} // rule hasn't ended; followed by...

    // As we match arguments, we count them in order to validate the call:
    LPAREN ( expr {$argsCount++;} ( COMMA expr {$argsCount++;} )* )? RPAREN {

// After matching the whole function call, validate the function name and arguments:
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

ifStat : IF NL* LPAREN NL* cond NL* RPAREN NL* LBRACE NL* block NL* RBRACE ;

cond : expr NL* ( EQ | NE | LE | LT | GE | GT ) NL* expr ;

expr
  : expr ( MUL | DIV | MOD ) expr
  | expr ( ADD | SUB ) expr
  | ( ADD | SUB )? LPAREN expr RPAREN // parenthesized expression
  | ( ADD | SUB )? fncall
  | ( ADD | SUB )? string
  | ( ADD | SUB )? LITERAL
  | ( ADD | SUB )? ID
  ;

string : QUOTE ( ESC_CHAR | STR_CHAR | fncall | (IN_STR_LBRACK expr RBRACK) )* IN_STR_QUOTE ;
