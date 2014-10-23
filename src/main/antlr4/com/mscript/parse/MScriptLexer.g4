/**
 * The MScript lexer is defined separately since we are using lexical modes which are only allowed in lexer grammars.
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Sep 27, 2014
 */
lexer grammar MScriptLexer;

// ---------- Default "mode": everything OUTSIDE a quoted string ----------

ASSIGN : '=' ;

MUL : '*' ;
DIV : '/' ;
MOD : '%' ;
ADD : '+' ;
SUB : '-' ;

SIGIL : '$' -> pushMode(IN_FNC) ;
DOT : '.' ;

LPAREN : '(' -> pushMode(DEFAULT_MODE) ;
RPAREN : ')' -> popMode ;
RBRACK : ']' -> popMode ;
LBRACE : '{' ;
RBRACE : '}' ;

EQ : '==' ;
NE : '!=' ;
LE : '<=' ;
LT : '<' ;
GE : '>=' ;
GT : '>' ;

COMMA : ',' ;
QUOTE : '\'' -> pushMode(IN_STR) ;

IF : 'if' ;
ELSE : 'else' ;
WHILE : 'while' ;

LITERAL : BOOLEAN | NUMBER ;

BOOLEAN : 'true' | 'false' ;

NUMBER : INT ( DOT INT )? | DOT INT ;

fragment INT : '0' | [1-9] [0-9]* ;

// Keep ID definition AFTER LITERALs so that for example, true would be interpreted as a boolean literal and not an ID.
// We do not currently handle / cover "java letters" (characters) above 0xFF and UTF-16 surrogate pairs encodings from
// U+10000 to U+10FFFF; if needed, find missing bits at http://github.com/antlr/grammars-v4/blob/master/java8/Java8.g4
ID : [a-zA-Z_] [a-zA-Z0-9_]* ;

// Grouping several separators into one provides for smaller automatically generated syntax trees
SEPARATORS : SP ( WS? SP )* ;

// In default mode, skip multi-line and single line comments and white spaces, other than new lines
SKIP : ( '/*' .*? '*/' | '//' ~[\r\n]* | WS ) -> skip ;

fragment SP : '\r'? '\n' | '\r' /* on mac */ | ';' ;

fragment WS : [ \t\f]+ ;

// ---------- INside a quoted STRing ----------
mode IN_STR;

IN_STR_QUOTE : '\'' -> popMode ;

IN_STR_LBRACK : '[' -> pushMode(DEFAULT_MODE) ;

IN_STR_SIGIL : '$' -> type(SIGIL), pushMode(IN_FNC) ;

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

// ---------- INside a FuNction Call ----------
mode IN_FNC;

IN_FNC_DOT : '.' -> type(DOT) ;

IN_FNC_ID : [a-zA-Z_] [a-zA-Z0-9_]* -> type(ID) ;

IN_FNC_LPAREN : '(' -> type(LPAREN), popMode, pushMode(DEFAULT_MODE) ;
