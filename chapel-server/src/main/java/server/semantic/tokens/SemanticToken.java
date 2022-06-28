package server.semantic.tokens;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SemanticToken {
    public int line;
    public int startChar;
    public final int length;
    public final int tokenType;
    public final int tokenModifiers;

    public SemanticToken(int line, int startChar, int length, int tokenType, int tokenModifiers) {
        this.line = line;
        this.startChar = startChar;
        this.length = length;
        this.tokenType = tokenType;
        this.tokenModifiers = tokenModifiers;
    }
    public ArrayList<Integer> toArray() {
        return new ArrayList<>(List.of(line, startChar, length, tokenType, tokenModifiers));
    }

//    @Override
//    public int compareTo(SemanticToken s2) {
//        var s1 = this;
//        return s1.line - s2.line == 0 ? s1.startChar - s2.startChar : s1.line - s2.line;
//    }
}
