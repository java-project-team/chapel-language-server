package Lexer;

import java.util.regex.Pattern;

public class TemplateToken {
    String type;
    public final Pattern pattern;

    TemplateToken(String type, String regex) {
        this.type = type;
        pattern = Pattern.compile(regex);
    }
}
