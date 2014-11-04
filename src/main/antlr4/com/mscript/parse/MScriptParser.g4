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

@parser::members { // members of the generated parser;

    /**
     * The current level of loop nesting (0 for top level statements). Used, for example, to determine at parse time
     * whether a break or continue statement can be accepted.
     */
    protected int loopDepth;

}

script : block? EOF ;

block : ( NEWLN | SEMIC )* stat ( ( NEWLN | SEMIC )+ stat? )* ;

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

ifStat
  : IF NEWLN* LPAREN NEWLN* cond NEWLN* RPAREN NEWLN*
    ( LBRACE ( block? | ( NEWLN | SEMIC )* ) RBRACE | stat ( NEWLN+ | SEMIC )? )
    ( NEWLN* ELSE NEWLN*  // optional ELSE branch
      ( LBRACE ( block? | ( NEWLN | SEMIC )* ) RBRACE | stat ( NEWLN+ | SEMIC )? ) )? ;

cond
  : expr NEWLN* ( EQ | NE | LE | LT | GE | GT ) NEWLN* expr
  | expr // in order to allow statements like while (v) { ... } or if ('true') { ... }
  ;

expr
  : expr ( MUL | DIV | MOD ) expr
  | expr ( ADD | SUB ) expr
  | ( ADD | SUB )? LPAREN expr RPAREN // parenthesized expression
  | ( ADD | SUB )? fncall
  | ( ADD | SUB )? string
  | ( ADD | SUB )? BOOLEAN
  | ( ADD | SUB )? NUMBER
  | ( ADD | SUB )? ID
  ;

string : QUOTE ( STR_CHARS | fncall | IN_STR_LBRACK expr RBRACK )* IN_STR_QUOTE ;
