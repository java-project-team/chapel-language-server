package Lexer;

import java.util.List;

public interface Lexer {
    List<Token> ran(String path);
    void restart();
}