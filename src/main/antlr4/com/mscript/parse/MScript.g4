grammar MScript;

//////////////////////////////////
//
// MScript Parser Specification
//
//////////////////////////////////

script : STAT_SEPARATOR* stat? ( STAT_SEPARATOR stat )* STAT_SEPARATOR* EOF ;

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

// We do not currently handle / cover "java letters" (characters) above 0xFF and UTF-16 surrogate pairs encodings from
// U+10000 to U+10FFFF; if needed, find missing bits at http://github.com/antlr/grammars-v4/blob/master/java8/Java8.g4
ID : [a-zA-Z_] [a-zA-Z0-9_]* ;

LITERAL
  : BOOLEAN
  | NUMBER
  | STRING
  ;

BOOLEAN : 'true' | 'false' ;

NUMBER : INT ( '.' INT )? ;

fragment INT : [0-9]+ ;

STRING : '\'' ~[']* '\'' ;

STAT_SEPARATOR
  : '\r'? '\n'
  | ';'
  ;

// Skip multi-line and single line comments and white spaces, other than new lines
SKIP : ( '/*' .*? '*/' | '//' ~[\r\n]* | [ \t\f]+ ) -> skip ;

// Interesting / similar grammars to study:
//   http://github.com/antlr/grammars-v4/blob/master/ecmascript/ECMAScript.g4
//   http://github.com/antlr/grammars-v4/blob/master/java8/Java8.g4
//   http://github.com/antlr/grammars-v4/blob/master/python3/Python3.g4
