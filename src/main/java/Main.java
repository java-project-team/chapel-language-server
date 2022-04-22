import Lexer.Token;
import Lexer.LexerScala;

import java.io.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        LexerScala lexer = new LexerScala();
        for (int i = 1; i <= 5; i++) {
            lexer.restart();
            List<Token> list = lexer.ran("./src/main/java/Lexer/Test/" + i + ".txt");
            System.out.print("Test " + i + "\n");
            try {
                new PrintWriter("./src/main/java/Lexer/Test/res" + i + ".txt").close();
            }
            catch (FileNotFoundException e) {
                System.err.print("The res file cannot be opened\n");
            }
            for (Token t : list) {
                t.print("./src/main/java/Lexer/Test/res" + i + ".txt");
            }
        }
    }
}
