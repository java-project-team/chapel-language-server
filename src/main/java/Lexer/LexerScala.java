package Lexer;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.*;

public class LexerScala implements Lexer {
    static class Pair {
        int first, second;

        Pair(int first, int second) {
            this.first = first;
            this.second = second;
        }
    }

    List<TemplateToken> tokens;
    Map<Integer, List<Pair>> checked;

    public LexerScala() {
        tokens = new ArrayList<TemplateToken>();
        // они стоят по приоритету, так что порядок важен
        tokens.add(new TemplateToken("COMM", "//.+"));
        tokens.add(new TemplateToken("OPEN_COMM", "/\\*")); // многострочная фигня, попыталась обработать, но в номер строки не знаю, что написать, так что мб пусть этим парсер в начале где-нибудь займётся
        tokens.add(new TemplateToken("CLOSE_COMM", "\\*/"));
        tokens.add(new TemplateToken("STRING", "\"[^\"]+\""));
        tokens.add(new TemplateToken("SPACE", "\\s"));

        tokens.add(new TemplateToken("KEY_TRAIT", "\\btrait\\b"));
        tokens.add(new TemplateToken("KEY_VAR", "\\bvar\\b"));
        tokens.add(new TemplateToken("KEY_VAL", "\\bval\\b"));
        tokens.add(new TemplateToken("KEY_LAZY", "\\blazy\\b"));
        tokens.add(new TemplateToken("KEY_IMPORT", "\\bimport\\b"));
        tokens.add(new TemplateToken("KEY_CLASS", "\\bclass\\b"));
        tokens.add(new TemplateToken("KEY_PACKAGE", "\\bpackage\\b"));
        tokens.add(new TemplateToken("KEY_DEF", "\\bdef\\b"));
        tokens.add(new TemplateToken("KEY_OBJECT", "\\bobject\\b"));
        tokens.add(new TemplateToken("KEY_IMPLICIT", "\\bimplicit\\b"));
        tokens.add(new TemplateToken("KEY_EXTENDS", "\\bextends\\b"));
        tokens.add(new TemplateToken("KEY_WITH", "\\bwith\\b"));
        tokens.add(new TemplateToken("KEY_NEW", "\\bnew\\b"));
        tokens.add(new TemplateToken("KEY_FINAL", "\\bfinal\\b"));
        tokens.add(new TemplateToken("KEY_OVERRIDE", "\\boverride\\b"));
        tokens.add(new TemplateToken("KEY_WHILE", "\\bwhile\\b"));
        tokens.add(new TemplateToken("KEY_PRIVATE", "\\bprivate\\b"));
        tokens.add(new TemplateToken("KEY_ABSTRACT", "\\babstract\\b"));
        tokens.add(new TemplateToken("KEY_PUBLIC", "\\bpublic\\b"));
        tokens.add(new TemplateToken("KEY_PROTECTED", "\\bprotected\\b"));
        tokens.add(new TemplateToken("KEY_THIS", "\\bthis\\b"));
        tokens.add(new TemplateToken("KEY_IF", "\\bif\\b"));
        tokens.add(new TemplateToken("KEY_ELSE", "\\belse\\b"));
        tokens.add(new TemplateToken("KEY_DO", "\\bdo\\b"));
        tokens.add(new TemplateToken("KEY_MATCH", "\\bmatch\\b"));
        tokens.add(new TemplateToken("KEY_CASE", "\\bcase\\b"));

        tokens.add(new TemplateToken("NAME_VAR", "[a-zA-Z][a-zA-Z0-9]+"));
        tokens.add(new TemplateToken("REAL", "[+-]?(\\d*)\\.\\d+"));
        tokens.add(new TemplateToken("INTEGER", "[+-]?\\d+"));
        tokens.add(new TemplateToken("BIT_TRIPLE_ANGLE_BRACKET_RIGHT", ">>>"));
        tokens.add(new TemplateToken("BIT_DOUBLE_ANGLE_BRACKET_RIGHT", ">>"));
        tokens.add(new TemplateToken("BIT_DOUBLE_ANGLE_BRACKET_LEFT", "<<"));
        tokens.add(new TemplateToken("LEG", "<="));
        tokens.add(new TemplateToken("ARROW_RIGHT", "=>"));
        tokens.add(new TemplateToken("GEQ", ">="));
        tokens.add(new TemplateToken("EQ", "=="));
        tokens.add(new TemplateToken("NEQ", "!="));
        tokens.add(new TemplateToken("PLUS_ASSIGN", "\\+="));
        tokens.add(new TemplateToken("MINUS_ASSIGN", "-="));
        tokens.add(new TemplateToken("MUL_ASSIGN", "\\*="));
        tokens.add(new TemplateToken("DIV_ASSIGN", "/="));
        tokens.add(new TemplateToken("MOD_ASSIGN", "%="));
        tokens.add(new TemplateToken("AND", "&&"));
        tokens.add(new TemplateToken("OR", "\\|\\|"));
        tokens.add(new TemplateToken("MINUS", "-"));
        tokens.add(new TemplateToken("PLUS", "\\+"));
        tokens.add(new TemplateToken("MUL", "\\*"));
        tokens.add(new TemplateToken("DIV", "/"));
        tokens.add(new TemplateToken("MOD", "%"));
        tokens.add(new TemplateToken("ANNOTATION", "@"));
        tokens.add(new TemplateToken("NOT", "!"));
        tokens.add(new TemplateToken("LESS", "<"));
        tokens.add(new TemplateToken("GREAT", ">"));
        tokens.add(new TemplateToken("COLON", ":"));
        tokens.add(new TemplateToken("OPEN_PARENTHESIS", "\\("));
        tokens.add(new TemplateToken("CLOSE_PARENTHESIS", "\\)"));
        tokens.add(new TemplateToken("OPEN_CURLY_BRACKET", "\\{"));
        tokens.add(new TemplateToken("CLOSE_CURLY_BRACKET", "\\}"));
        tokens.add(new TemplateToken("OPEN_SQUARE_BRACKET", "\\["));
        tokens.add(new TemplateToken("CLOSE_SQUARE_BRACKET", "\\]"));
        tokens.add(new TemplateToken("SEMI", ";"));
        tokens.add(new TemplateToken("COMMA", ","));
        tokens.add(new TemplateToken("POINT", "\\."));
        tokens.add(new TemplateToken("BIT_AND", "&"));
        tokens.add(new TemplateToken("BIT_XOR", "\\^"));
        tokens.add(new TemplateToken("BIT_OR", "\\|"));
        tokens.add(new TemplateToken("BIT_WTF", "~"));
        tokens.add(new TemplateToken("ASSIGN", "="));

        checked = new HashMap<>();
    }

    private List<Token> find(List<String> lines, TemplateToken type) {
        AtomicInteger lineNumber = new AtomicInteger();
        ArrayList<Token> resList = new ArrayList<>();
        lines.forEach(line -> {
            Matcher matcher = type.pattern.matcher(line);
            int k = lineNumber.incrementAndGet();
            while (matcher.find()) {
                int start = matcher.start(), end = matcher.end();
                boolean flag = true;

                checked.computeIfAbsent(k, x -> new ArrayList<>());

                List<Pair> list = checked.get(k);
                for (Pair p : list) {
                    if (p.first <= start && end <= p.second) {
                        flag = false;
                        break;
                    }
                }

                if (flag) {
                    checked.get(k).add(new Pair(start, end));
                    resList.add(new Token(type.type, line.substring(start, end), k, start, end));
                }
            }
        });
        return resList;
    }

    @Override
    public List<Token> ran(String path) {
        List<String> lines = null;
        try {
            lines = Files.lines(Paths.get(path)).toList();
        } catch (IOException e) {
            System.err.print("The file cannot be opened\n");
            return null;
        }

        List<Token> res = new ArrayList<Token>();
        for (TemplateToken type : tokens) {
            res.addAll(find(lines, type));
        }

        res.sort((a, b) -> {
            if (a.lineNumber != b.lineNumber) {
                return a.lineNumber - b.lineNumber;
            }
            if (a.posBegin != b.posBegin) {
                return a.posBegin - b.posBegin;
            }
            System.err.print("Этого не должно было произойти, сочувствую: " + a.lineNumber + ", " + a.posBegin + "; a = " + a.posEnd + "; b = " + b.posEnd + "\n");
            if (a.posEnd != b.posEnd) {
                return a.posEnd - b.posEnd;
            }
            return 0;
        });

        return res;
    }

    @Override
    public void restart() {
        checked = new HashMap<>();
    }
}