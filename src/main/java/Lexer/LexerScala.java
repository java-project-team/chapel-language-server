package Lexer;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.*;
import java.util.stream.*;

public class LexerScala implements Lexer {
    HashMap<TypeTokens, String> regFromToken;

    LexerScala() {
        regFromToken = new HashMap<>();
        regFromToken.put(TypeTokens.STR, "\"[^\"\\\\]+(?:\\\\.[^\"\\\\]*)*\""); // писала в https://regex101.com/, так скопировалось, TODO пока не проверяла
        regFromToken.put(TypeTokens.COMMENT, "\\\\.*"); // TODO Пока только однострочный
        regFromToken.put(TypeTokens.NUM, "[+-]?(\\d*\\.\\d+|\\d+\\.\\d*|\\d+)"); // писала в https://regex101.com/, так скопировалось, TODO пока не проверяла
    }

    private Stream<Token> find(Stream<String> stream, TypeTokens type) {
        Pattern pattern = Pattern.compile(regFromToken.get(type));
        AtomicInteger lineNumber = new AtomicInteger();
        return stream.map(line -> {
                    ArrayList<Token> resList = new ArrayList<>();
                    Matcher matcher = pattern.matcher(line);
                    while (matcher.find()) {
                        resList.add(new Token(lineNumber.incrementAndGet(),
                                matcher.start(),
                                matcher.end(),
                                line.substring(matcher.start(), matcher.end()),
                                type));
                    }
                    return resList.stream();
                })
                .reduce(Stream::concat)
                .orElse(Stream.of());
    }

    @Override
    public List<Token> ran(String path) {
        Stream<String> stream = null;
        try {
            stream = Files.lines(Paths.get(path));
        } catch (IOException e) {
            System.err.print("The file cannot be opened");
            return null;
        }

        Stream<Token> res = Stream.of();
        for (TypeTokens type : regFromToken.keySet()) {
            res = Stream.concat(res, find(stream, type));
        }

        return res.toList();
    }
}
