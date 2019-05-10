@skip CommentOrWhitespace {
    FILE ::= ">>" NAME DESCRIPTION NEWLINES ENTRY*;
    NAME ::= StringIdent;
    DESCRIPTION ::= String;
    ENTRY ::= "("  WORDNAME "," CLUE "," DIRECTION "," ROW "," COL ")";
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
Comment::= "\/\/" [^\r\n]* "\n"?;
CommentOrWhitespace::=(Comment | whitespace);