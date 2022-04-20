package Lexer;

import java.util.regex.Pattern;
import java.util.List;

public interface Lexer {
    enum TypeTokens {
        MINUS ("-"),
        PLUS ("\\+"),
        MUL ("\\*"),
        DIV ("/"),
        MOD ("%"),
        KEY_ARGS ("args"),
        KEY_VAR ("var"),
        KEY_VAL ("val"),
        KEY_LAZY ("lazy"),
        KEY_DEF ("def"),
        KEY_OBJECT ("object"),
        NOT ("!"),
        LESS ("<"),
        LEG ("<="),
        GT (">"),
        GEQ (">="),
        EQ ("=="),
        NEQ ("!="),
        AND ("&&"),
        OR ("\\|\\|"),
        COLON (":"),
        OPEN_PARENTHESIS ("\\("),
        CLOSE_PARENTHESIS ("\\)"),
        OPEN_CURLY_BRACKET ("\\{"),
        CLOSE_CURLY_BRACKET ("\\}"),
        OPEN_SQUARE_BRACKET ("\\["),
        CLOSE_SQUARE_BRACKET ("\\]"),
        SEMI (";"),
        COMMA (","),
        POINT ("\\."),
        BIT_AND ("&"),
        BIT_OR ("\\|"),
        BIT_DOUBLE_ANGLE_BRACKET_RIGHT (">>"),
        BIT_DOUBLE_ANGLE_BRACKET_LEFT ("<<"),
        BIT_TRIPLE_ANGLE_BRACKET_RIGHT (">>>"),
        BIT_WTF ("~"),
        COMM ("\\\\"),
        OPEN_COMM ("/\\*"),
        CLOSE_COMM ("\\*/"),
        ASSIGN ("="),
        STRING ("\"[^\"]+\""),
        INTEGER ("[+-]?\\d+"),
        REAL ("[+-]?(\\d*)\\.\\d+"),
        KEY_IF ("if"),
        KEY_ELSE ("else"),
        KEY_DO ("do"),
        NAME_VAR ("[a-zA-Z][a-zA-Z0-9]+");

        public final Pattern pattern;

        TypeTokens(String regex) {
            pattern = Pattern.compile("^" + regex);
        }
    }

    List<Token> ran(String path);
}