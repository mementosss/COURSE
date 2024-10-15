package analizator.lexer;

public enum Type {
    ID,
    INTEGER,
    FLOAT,
    EQUAL,
    PLUS,
    MULT,
    NOT,
    KEYWORD,
    BEGIN_PROG,
    END_PROG,
    BEGIN_COMPLEX,
    END_COMPLEX,
    TYPE,
    COMMA,
    SEMICOLON,
    LINE_BREAK,
    COLON,
    ASSIGN,
    IF,
    THEN,
    ELSE,
    FOR,
    TO,
    DO,
    WHILE,
    READ,
    WRITE,
    TRUE,
    FALSE,
    BEGIN_COMMENT,
    END_COMMENT,
    ERR,
    LEFT_PAREN,
    RIGHT_PAREN,
    TYPE_I,
    TYPE_F,
    TYPE_B,
    MINUS,
    DIVIDE,
    AND,
    OR,
    NEQ,
    GEQ,
    GREATER,
    LEQ,
    LESS,
    UNARY,
    VAR,
    END_DESCRIPTION,
    DESCRIPTION,
    IN,
    EMPTY;

    public static Type getType(String token) {
        return switch (token) {
            case "%" -> TYPE_I;
            case "!" -> TYPE_F;
            case "$" -> TYPE_B;
            case "NE" -> NEQ;
            case "EQ" -> EQUAL;
            case "LT" -> LESS;
            case "LE" -> LEQ;
            case "GT" -> GREATER;
            case "GE" -> GEQ;
            case "plus" -> PLUS;
            case "min" -> MINUS;
            case "or" -> OR;
            case "mult" -> MULT;
            case "div" -> DIVIDE;
            case "and" -> AND;
            case "~" -> UNARY;
            case "as" -> ASSIGN;
            case "program" -> BEGIN_PROG;
            case "var" -> VAR;
            case "{" -> BEGIN_COMMENT;
            case "}" -> END_COMMENT;
            case "," -> COMMA;
            case "read" -> READ;
            case "write" -> WRITE;
            case "for" -> FOR;
            case "while" -> WHILE;
            case "true" -> TRUE;
            case "false" -> FALSE;
            case "then" -> THEN;
            case "[" -> BEGIN_COMPLEX;
            case "]" -> END_COMPLEX;
            case "end." -> END_PROG;
            case "begin" -> END_DESCRIPTION;
            case "(" -> LEFT_PAREN;
            case ")" -> RIGHT_PAREN;
            case "to" -> TO;
            case "in" -> IN;
            case ":" -> COLON;
            case "if" -> IF;
            case "do" -> DO;
            case "else" -> ELSE;
            default -> EMPTY;
        };
    }
}