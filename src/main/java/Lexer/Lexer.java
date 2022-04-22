package Lexer;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.*;

public class Lexer {
    static class Pair {
        int first, second;

        Pair(int first, int second) {
            this.first = first;
            this.second = second;
        }
    }

    List<TemplateToken> tokens;
    Map<Integer, List<Pair>> checked;

    // Токены должны быть отсорчены по приоритету, порядок важен
    public Lexer(List<TemplateToken> tokens) {
        this.tokens = tokens;
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

    public List<Token> ran(String path) {
        List<String> lines;
        try {
            lines = Files.lines(Paths.get(path)).toList();
        } catch (IOException e) {
            System.err.print("The file cannot be opened\n");
            return null;
        }

        List<Token> res = new ArrayList<>();
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

    public void restart() {
        checked = new HashMap<>();
    }
}