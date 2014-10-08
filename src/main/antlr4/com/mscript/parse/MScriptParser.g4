/**
 * MScript Parser Definition
 */
parser grammar MScriptParser;

options { tokenVocab=MScriptLexer; }

script : STAT_SEPARATOR* stat? ( STAT_SEPARATOR stat? )* EOF ;

stat
  : assign
  ;

assign : ID ASSIGN expr ;

expr
  : string
  | LITERAL
  ;

string : QUOTE ( ESC_CHAR | STR_CHAR )* IN_STR_QUOTE ;
