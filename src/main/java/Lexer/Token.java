package Lexer;

public class Token {
    int lineNumber = 0, posBegin = 0, posEnd = 0;
    String val = "";
    Lexer.Lexer.TypeTokens type;

    Token(int lineNumber, int posBegin, int posEnd, String val, Lexer.Lexer.TypeTokens type) {
        this.lineNumber = lineNumber;
        this.posBegin = posBegin;
        this.posEnd = posEnd;
        this.val = val;
        this.type = type;
    }

    public void print() {
        System.out.printf("Line number = %d, pos begin = %d, pos end = %d, val = %s\n", lineNumber, posBegin, posEnd, val);
    }
}