import java.util.List;

public interface Lexer {
    enum TypeTokens {
        NUM,
        COMMENT,
        STR
    } // TODO ещё очев что-то надо, но пока с самим языком не разбиралась

    public class Token {
        int lineNumber = 0, posBegin = 0, posEnd = 0;
        String val = "";
        TypeTokens type;
    }

    List<Token> ran(String path);
}
