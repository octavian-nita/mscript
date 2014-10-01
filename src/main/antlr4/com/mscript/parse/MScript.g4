grammar MScript;

script : STAT_SEPARATOR* stat? (STAT_SEPARATOR stat)* STAT_SEPARATOR* EOF ;

stat
  : assign
  ;

assign : ID '=' expr ;

expr : LITERAL;

ID : ;

LITERAL : ;

NL : '\r' ? '\n' ;

STAT_SEPARATOR : NL | ';' ;

// Skip multi-line and single line comments and white spaces, other than new lines
SKIP
  : '/*' .*? '*/'
  | '//' ~[\r\n]*
  | [ \t\f]+ -> skip
  ;

// Interesting / similar grammars to study:
//   http://github.com/antlr/grammars-v4/blob/master/ecmascript/ECMAScript.g4
//   http://github.com/antlr/grammars-v4/blob/master/java8/Java8.g4
//   http://github.com/antlr/grammars-v4/blob/master/python3/Python3.g4
