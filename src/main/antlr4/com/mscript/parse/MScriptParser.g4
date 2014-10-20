/**
 * MScript Parser Definition
 */
parser grammar MScriptParser;

options { tokenVocab=MScriptLexer; }

@header {
import com.mscript.Function;
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
locals [int argc = 0;]
  : SIGIL ( ID DOT )? ID LPAREN ( expr { $argc++; } ( COMMA expr { $argc++; } )* )? RPAREN {

    // Validate function existence:
    if (Function.check()) {
    }

  } ;

string : QUOTE ( ESC_CHAR | STR_CHAR | fncall | (IN_STR_LBRACK expr RBRACK) )* IN_STR_QUOTE ;
