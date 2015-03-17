/**
 * The MScript lexer is defined separately since we are using lexical modes which are only allowed in lexer grammars.
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Sep 27, 2014
 */
lexer grammar MScriptLexer;

// ---------- Default "mode": everything OUTSIDE a quoted string or a function call ----------

// Arithmetic operators
MUL : '*' ;
DIV : '/' ;
MOD : '%' ;
ADD : '+' ;
SUB : '-' ;

// Comparison operators
EQ : '==' ;
NE : '!=' ;
LE : '<=' ;
LT : '<'  ;
GE : '>=' ;
GT : '>'  ;

// Function call-related tokens
SIGIL : '$' -> pushMode(IN_FNC) ;        // upon encountering a sigil token, switch to "function call" (matching) mode
DOT   : '.' ;
COMMA : ',' ;

LPAREN : '(' -> pushMode(DEFAULT_MODE) ; // within parentheses (either from a function call or simple operation
                                         // grouping), we switch in (or push a) default matching mode once more;
RPAREN : ')' -> popMode ;                // we ended either a function call or simple operation grouping so we go back
                                         // to previous matching mode

QUOTE  : '\'' -> pushMode(IN_STR) ;
RBRACK : ']'  -> popMode ;

LBRACE : '{' ;
RBRACE : '}' ;

ASSIGN : '=' ;
PIPE   : '|' ;

// Keywords
IF       : 'if'    ;
ELSE     : 'else'  ;
WHILE    : 'while' ;
BREAK    : 'break' ;
CONTINUE : 'continue' ;

// Literals, apart from string since this one is defined in the parser
FLOAT   : INTEGER DOT INTEGER? | DOT INTEGER ; // no scientific notation for the moment
INTEGER : '0' | [1-9] [0-9]* ; // could have been a fragment but we need it as a token for maxLoopNum
BOOLEAN : 'true' | 'false' ;

// Keep ID definition AFTER literals so that for example, true would be interpreted as a BOOLEAN literal and not an ID.
// We do not currently handle / cover "java letters" (characters) above 0xFF and UTF-16 surrogate pairs encodings from
// U+10000 to U+10FFFF; if needed, find missing bits at http://github.com/antlr/grammars-v4/blob/master/java8/Java8.g4
ID : [a-zA-Z_] [a-zA-Z0-9_]* ;

COMM : '/*' .*? '*/' | '//' ~[\r\n]* ; // comments will be kept in the AST

// Statement separators kept separate to allow newlines between tokens like IF and ( without an marking end of statement
SEMI : ';' ;

NL : ( '\r'? '\n' ) | '\r' /* mac */ ;

WS : [ \t\f]+ -> skip ; // in default mode, skip white spaces other than new lines

// ---------- INside a quoted STRing "mode" ----------

mode IN_STR;

IN_STR_QUOTE  : '\'' -> popMode ;

IN_STR_LBRACK : '['  -> pushMode(DEFAULT_MODE) ;

IN_STR_SIGIL  : '$'  -> type(SIGIL), pushMode(IN_FNC) ;

IN_STR_CHARS  : ( ~( '\\' | '\'' | '$' | '[' | ']' ) | ESC_CHAR )+ ;

// Allowed escape sequences: \\, \', \$, \[, \], \n, \r, \t
fragment ESC_CHAR : '\\\\' | '\\\'' | '\\$' | '\\[' | '\\]' | '\\n' | '\\r' | '\\t' ;

// ---------- INside a FuNction Call "mode" ----------

mode IN_FNC;

IN_FNC_DOT    : '.' -> type(DOT) ;

IN_FNC_ID     : [a-zA-Z_] [a-zA-Z0-9_]* -> type(ID) ;

IN_FNC_LPAREN : '(' -> type(LPAREN), popMode, pushMode(DEFAULT_MODE) ;
