grammar MScript;

script : (NL | stat)* EOF ;

stat : 'hello' ;

WS : [ \t\f]+ -> skip ;

NL : '\r' ? '\n' ;

ML_COMMENT : '/*' .*? '*/' -> skip ;

SL_COMMENT : '//' ~[\r\n]* -> skip ;

// Inspiration:
//   http://github.com/antlr/grammars-v4/blob/master/ecmascript/ECMAScript.g4
//   http://github.com/antlr/grammars-v4/blob/master/java8/Java8.g4
