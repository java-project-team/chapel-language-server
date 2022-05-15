package lexer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        // TestScala
        {
            ArrayList<TemplateToken> tokensScala = new ArrayList<>();
            tokensScala.add(new TemplateToken("COMM", "//.+"));
            tokensScala.add(new TemplateToken("OPEN_COMM", "/\\*")); // многострочная фигня, попыталась обработать, но в номер строки не знаю, что написать, так что мб пусть этим парсер в начале где-нибудь займётся
            tokensScala.add(new TemplateToken("CLOSE_COMM", "\\*/"));
            tokensScala.add(new TemplateToken("STRING", "\"[^\"]+\""));
            tokensScala.add(new TemplateToken("SPACE", "\\s"));
            tokensScala.add(new TemplateToken("KEY_TRAIT", "\\btrait\\b"));
            tokensScala.add(new TemplateToken("KEY_VAR", "\\bvar\\b"));
            tokensScala.add(new TemplateToken("KEY_VAL", "\\bval\\b"));
            tokensScala.add(new TemplateToken("KEY_LAZY", "\\blazy\\b"));
            tokensScala.add(new TemplateToken("KEY_IMPORT", "\\bimport\\b"));
            tokensScala.add(new TemplateToken("KEY_CLASS", "\\bclass\\b"));
            tokensScala.add(new TemplateToken("KEY_PACKAGE", "\\bpackage\\b"));
            tokensScala.add(new TemplateToken("KEY_DEF", "\\bdef\\b"));
            tokensScala.add(new TemplateToken("KEY_OBJECT", "\\bobject\\b"));
            tokensScala.add(new TemplateToken("KEY_IMPLICIT", "\\bimplicit\\b"));
            tokensScala.add(new TemplateToken("KEY_EXTENDS", "\\bextends\\b"));
            tokensScala.add(new TemplateToken("KEY_WITH", "\\bwith\\b"));
            tokensScala.add(new TemplateToken("KEY_NEW", "\\bnew\\b"));
            tokensScala.add(new TemplateToken("KEY_FINAL", "\\bfinal\\b"));
            tokensScala.add(new TemplateToken("KEY_OVERRIDE", "\\boverride\\b"));
            tokensScala.add(new TemplateToken("KEY_WHILE", "\\bwhile\\b"));
            tokensScala.add(new TemplateToken("KEY_PRIVATE", "\\bprivate\\b"));
            tokensScala.add(new TemplateToken("KEY_ABSTRACT", "\\babstract\\b"));
            tokensScala.add(new TemplateToken("KEY_PUBLIC", "\\bpublic\\b"));
            tokensScala.add(new TemplateToken("KEY_PROTECTED", "\\bprotected\\b"));
            tokensScala.add(new TemplateToken("KEY_THIS", "\\bthis\\b"));
            tokensScala.add(new TemplateToken("KEY_IF", "\\bif\\b"));
            tokensScala.add(new TemplateToken("KEY_ELSE", "\\belse\\b"));
            tokensScala.add(new TemplateToken("KEY_DO", "\\bdo\\b"));
            tokensScala.add(new TemplateToken("KEY_MATCH", "\\bmatch\\b"));
            tokensScala.add(new TemplateToken("KEY_CASE", "\\bcase\\b"));
            tokensScala.add(new TemplateToken("NAME_VAR", "[a-zA-Z][a-zA-Z0-9]+"));
            tokensScala.add(new TemplateToken("REAL", "[+-]?(\\d*)\\.\\d+"));
            tokensScala.add(new TemplateToken("INTEGER", "[+-]?\\d+"));
            tokensScala.add(new TemplateToken("BIT_TRIPLE_ANGLE_BRACKET_RIGHT", ">>>"));
            tokensScala.add(new TemplateToken("BIT_DOUBLE_ANGLE_BRACKET_RIGHT", ">>"));
            tokensScala.add(new TemplateToken("BIT_DOUBLE_ANGLE_BRACKET_LEFT", "<<"));
            tokensScala.add(new TemplateToken("LEG", "<="));
            tokensScala.add(new TemplateToken("ARROW_RIGHT", "=>"));
            tokensScala.add(new TemplateToken("GEQ", ">="));
            tokensScala.add(new TemplateToken("EQ", "=="));
            tokensScala.add(new TemplateToken("NEQ", "!="));
            tokensScala.add(new TemplateToken("PLUS_ASSIGN", "\\+="));
            tokensScala.add(new TemplateToken("MINUS_ASSIGN", "-="));
            tokensScala.add(new TemplateToken("MUL_ASSIGN", "\\*="));
            tokensScala.add(new TemplateToken("DIV_ASSIGN", "/="));
            tokensScala.add(new TemplateToken("MOD_ASSIGN", "%="));
            tokensScala.add(new TemplateToken("AND", "&&"));
            tokensScala.add(new TemplateToken("OR", "\\|\\|"));
            tokensScala.add(new TemplateToken("MINUS", "-"));
            tokensScala.add(new TemplateToken("PLUS", "\\+"));
            tokensScala.add(new TemplateToken("MUL", "\\*"));
            tokensScala.add(new TemplateToken("DIV", "/"));
            tokensScala.add(new TemplateToken("MOD", "%"));
            tokensScala.add(new TemplateToken("ANNOTATION", "@"));
            tokensScala.add(new TemplateToken("NOT", "!"));
            tokensScala.add(new TemplateToken("LESS", "<"));
            tokensScala.add(new TemplateToken("GREAT", ">"));
            tokensScala.add(new TemplateToken("COLON", ":"));
            tokensScala.add(new TemplateToken("OPEN_PARENTHESIS", "\\("));
            tokensScala.add(new TemplateToken("CLOSE_PARENTHESIS", "\\)"));
            tokensScala.add(new TemplateToken("OPEN_CURLY_BRACKET", "\\{"));
            tokensScala.add(new TemplateToken("CLOSE_CURLY_BRACKET", "\\}"));
            tokensScala.add(new TemplateToken("OPEN_SQUARE_BRACKET", "\\["));
            tokensScala.add(new TemplateToken("CLOSE_SQUARE_BRACKET", "\\]"));
            tokensScala.add(new TemplateToken("SEMI", ";"));
            tokensScala.add(new TemplateToken("COMMA", ","));
            tokensScala.add(new TemplateToken("POINT", "\\."));
            tokensScala.add(new TemplateToken("BIT_AND", "&"));
            tokensScala.add(new TemplateToken("BIT_XOR", "\\^"));
            tokensScala.add(new TemplateToken("BIT_OR", "\\|"));
            tokensScala.add(new TemplateToken("BIT_WTF", "~"));
            tokensScala.add(new TemplateToken("ASSIGN", "="));

            Lexer lexerScala = new Lexer(tokensScala);
            for (int i = 1; i <= 5; i++) {
                lexerScala.restart();
                List<Token> list = lexerScala.ran("./src/main/java/Lexer/Test/" + i + ".txt");
                System.out.print("TestScala " + i + "\n");
                try {
                    new PrintWriter("./src/main/java/Lexer/Test/res" + i + ".txt").close();
                } catch (FileNotFoundException e) {
                    System.err.print("The res file cannot be opened\n");
                }
                for (Token t : list) {
                    t.printFile("./src/main/java/Lexer/Test/res" + i + ".txt");
                }
            }
        }
    }
}