/**
 * MScript Parser Definition
 */
parser grammar MScriptParser;

options { tokenVocab=MScriptLexer; }

script : STAT_SEPARATOR* stat? ( STAT_SEPARATOR stat? )* EOF ;

stat
  : assign
  | fncall
  ;

assign : ID ASSIGN expr ;

expr
  : expr ( MUL | DIV | MOD ) expr
  | expr ( ADD | SUB ) expr
  | LPAREN expr RPAREN // parenthesized expression
  | ADD expr           // e.g. +3
  | SUB expr           // e.g. -7
  | fncall
  | string
  | LITERAL
  | ID
  ;

string : QUOTE ( ESC_CHAR | STR_CHAR | fncall )* IN_STR_QUOTE ;

fncall : SIGIL ( ID DOT )? ID LPAREN ( expr ( COMMA expr )* )? RPAREN ;
