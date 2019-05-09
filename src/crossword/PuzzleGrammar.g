@skip whitespace {
    FILE ::= ">>" NAME (Comment)* DESCRIPTION (Comment)* NEWLINES (Comment)* ENTRY* (Comment)*;
    NAME ::= StringIdent (Comment)*;
    DESCRIPTION ::= String;
    ENTRY ::= "("  WORDNAME "," (Comment)*  CLUE "," (Comment)* DIRECTION "," (Comment)* ROW "," (Comment)* COL (Comment)* ")";
    WORDNAME ::= [a-z\-]+;
    CLUE ::= String;
    DIRECTION ::= "DOWN" | "ACROSS";
    ROW ::= Int;
    COL ::= Int;
}
NEWLINES ::= "\n"*;

String::= '"' ([^"\r\n\\] | '\\' [\\nrt] )* '"';
StringIdent ::= '"' [^"\r\n\t\\]* '"';
Int ::= [0-9]+;
spaces ::= [ ]*;
whitespace ::= [ \t\r\n]+;
Comment::= "\/\/" [^\r\n]* "\n"+;