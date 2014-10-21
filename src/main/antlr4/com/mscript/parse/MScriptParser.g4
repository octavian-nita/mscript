/**
 * MScript Parser Definition
 */
parser grammar MScriptParser;

options { tokenVocab=MScriptLexer; }

@header {
import com.mscript.Function;
import com.mscript.Function.CheckResult;
}

script : STAT_SEPARATOR* stat? ( STAT_SEPARATOR stat? )* EOF ;

stat
  : assign
  | fncall
  ;

assign : ID ASSIGN expr ;

expr
  : expr ( MUL | DIV | MOD ) expr
  | expr ( ADD | SUB ) expr
  | ( ADD | SUB )? LPAREN expr RPAREN // parenthesized expression
  | ( ADD | SUB )? fncall
  | ( ADD | SUB )? string
  | ( ADD | SUB )? LITERAL
  | ( ADD | SUB )? ID
  ;

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
        throw new RecognitionException("no such function: " + ($pluginName != null ? $pluginName + "." : "") +
                                       $functionName + " defined", this, _input, _localctx);
    case WRONG_NUM_OF_ARGS:
        throw new RecognitionException("function: " + ($pluginName != null ? $pluginName + "." : "") +
                                       $functionName + " cannot be called with " + $argsCount + " arguments",
                                       this, _input, _localctx);
    }

  }
  ;

string : QUOTE ( ESC_CHAR | STR_CHAR | fncall | (IN_STR_LBRACK expr RBRACK) )* IN_STR_QUOTE ;
