package lexer;

import java.util.regex.Pattern;

public class TemplateToken {
    String type;
    public final Pattern pattern;

    public TemplateToken(String type, String regex) {
        this.type = type;
        pattern = Pattern.compile(regex);
    }
}
