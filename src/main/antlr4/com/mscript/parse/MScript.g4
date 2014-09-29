grammar MScript;

script : stat*;

stat : 'hello';

WS : [ \t\f]+ -> skip;

NL : '\r' ? '\n';
