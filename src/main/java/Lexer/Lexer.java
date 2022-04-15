package Lexer;

import java.util.List;

public interface Lexer {
    enum TypeTokens {
        NUM,
        COMMENT,
        STR
    } // TODO ещё очев что-то надо, но пока с самим языком не разбиралась

    class Token {
        int lineNumber = 0, posBegin = 0, posEnd = 0;
        String val = "";
        TypeTokens type;

        Token(int lineNumber, int posBegin, int posEnd, String val, TypeTokens type) {
            this.lineNumber = lineNumber;
            this.posBegin = posBegin;
            this.posEnd = posEnd;
            this.val = val;
            this.type = type;
        }
    }

    List<Token> ran(String path);
}
