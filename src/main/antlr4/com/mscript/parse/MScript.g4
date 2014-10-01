grammar MScript;

//////////////////////////////////
//
// MScript Parser Specification
//
//////////////////////////////////

script : STAT_SEPARATOR* stat? ( STAT_SEPARATOR stat? )* EOF ;

stat
  : assign
  ;

assign : ID '=' expr ;

expr : LITERAL;

/////////////////////////////////
//
// MScript Lexer Specification
//
/////////////////////////////////

LITERAL
  : NUMBER
  | STRING
  | BOOLEAN
  ;

BOOLEAN : 'true' | 'false' ;

NUMBER : [+-]? ( INT ( '.' INT )? | '.' INT ) ;

fragment
INT
  : '0'
  | [1-9] [0-9]*
  ;

STRING : '\'' ( ESCAPE | ~['] )* '\'' ;

// \\, \', \$, \[, \], \n, \r, \t
fragment
ESCAPE
  : '\\\\'
  | '\\\''
  | '\\$'
  | '\\['
  | '\\]'
  | '\\n'
  | '\\r'
  | '\\t'
  ;

// Keep ID definition AFTER LITERALs (so that true would be interpreted as a boolean literal and not an ID!)

// We do not currently handle / cover "java letters" (characters) above 0xFF and UTF-16 surrogate pairs encodings from
// U+10000 to U+10FFFF; if needed, find missing bits at http://github.com/antlr/grammars-v4/blob/master/java8/Java8.g4
ID : [a-zA-Z_] [a-zA-Z0-9_]* ;

STAT_SEPARATOR
  : '\r'? '\n'
  | '\r' // mac
  | ';'
  ;

// Skip multi-line and single line comments and white spaces, other than new lines
SKIP : ( '/*' .*? '*/' | '//' ~[\r\n]* | [ \t\f]+ ) -> skip ;

// Interesting / similar grammars to study:
//   http://github.com/antlr/grammars-v4/blob/master/ecmascript/ECMAScript.g4
//   http://github.com/antlr/grammars-v4/blob/master/java8/Java8.g4
//   http://github.com/antlr/grammars-v4/blob/master/python3/Python3.g4
