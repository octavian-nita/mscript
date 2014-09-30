grammar MScript;

script : stat*;

stat : 'hello';

// \f: \u000C
// consider also \u000B\u00A0 (ECMAScript)
WS : [ \t\f]+ -> skip;

NL : '\r' ? '\n';

// Inspiration:
//   http://github.com/antlr/grammars-v4/blob/master/ecmascript/ECMAScript.g4
//   http://github.com/antlr/grammars-v4/blob/master/java8/Java8.g4
