@skip spaces { 
    FILE ::= ">>" NAME DESCRIPTION "\n"* ENTRY*;
}
@skip whitespace {
    NAME ::= StringIdent;
    DESCRIPTION ::= String;
    ENTRY ::= "("  WORDNAME ","  CLUE "," DIRECTION "," ROW "," COL ")";
    WORDNAME ::= [a-z\-]+;
    CLUE ::= String;
    DIRECTION ::= "DOWN" | "ACROSS";
    ROW ::= Int;
    COL ::= Int;
}
String::= '"' ([^"\r\n\\] | '\\' [\\nrt] )* '"';
StringIdent ::= '"' [^"\r\n\t\\]* '"';
Int ::= [0-9]+;
spaces ::= [ ];
whitespace ::= [ \t\r\n]+;