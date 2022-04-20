import Lexer.Lexer;
import Lexer.Token;
import Lexer.LexerScala;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        LexerScala lexer = new LexerScala();
        List<Token> list = lexer.ran("");
        for (Token t : list) {
            t.print();
        }

        System.out.println("Niggers");
    }
}
