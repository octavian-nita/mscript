grammar MScript;

text : ANY*;

ANY : ~[ \t]+;

WS : [ \t]+ -> skip;

NL : '\r' ? '\n';
