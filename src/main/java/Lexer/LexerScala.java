package Lexer;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.*;
import java.util.stream.*;

public class LexerScala implements Lexer {
    public LexerScala() {}

    private Stream<Token> find(Stream<String> stream, TypeTokens type) {
        AtomicInteger lineNumber = new AtomicInteger();
        return stream.map(line -> {
                    ArrayList<Token> resList = new ArrayList<>();
                    Matcher matcher = type.pattern.matcher(line);
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
        for (TypeTokens type : TypeTokens.values()) {
            res = Stream.concat(res, find(stream, type));
        }

        return res.toList();
    }
}