/**
 * The MScript lexer is defined separately since we are using lexical modes which are only allowed in lexer grammars.
 */
lexer grammar MScriptLexer;

// ---------- Default "mode": everything OUTSIDE a quoted string ----------

ASSIGN : '=' ;

QUOTE : '\'' -> pushMode(IN_STR) ;

LITERAL : BOOLEAN | NUMBER ;

BOOLEAN : 'true' | 'false' ;

fragment INT : '0' | [1-9] [0-9]* ;

NUMBER : [+-]? ( INT ( '.' INT )? | '.' INT ) ;

// Keep ID definition AFTER LITERALs so that, for example, true would be interpreted as a
// boolean literal and not an ID!

// We do not currently handle / cover "java letters" (characters) above 0xFF and UTF-16 surrogate pairs encodings from
// U+10000 to U+10FFFF; if needed, find missing bits at http://github.com/antlr/grammars-v4/blob/master/java8/Java8.g4
ID : [a-zA-Z_] [a-zA-Z0-9_]* ;

STAT_SEPARATOR
  : '\r'? '\n'
  | '\r' // mac
  | ';'
  ;

// In default mode, we skip multi-line and single line comments and white spaces, other than new lines
SKIP : ( '/*' .*? '*/' | '//' ~[\r\n]* | [ \t\f]+ ) -> skip ;

// ---------- Everything INside a quoted STRing ----------
mode IN_STR;

IN_STR_QUOTE : '\'' -> popMode ;

// Allowed escape sequences: \\, \', \$, \[, \], \n, \r, \t
ESC_CHAR
  : '\\\\'
  | '\\\''
  | '\\$'
  | '\\['
  | '\\]'
  | '\\n'
  | '\\r'
  | '\\t'
  ;

// Any character apart from: \, ', $, [, ]
STR_CHAR
  : ~('\\' | '\'' | '$' | '[' | ']')
  ;
