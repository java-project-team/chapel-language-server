package server.semantic.tokens;

import java.util.List;

public class SemanticTokensConstants {
    public static final List<String> LEGEND_TOKENS =
            List.of("namespace", "class", "enum", "variable", "enumMember", "function", "method", "keyword");
    public static final Integer NAMESPACE_TOKEN_INDEX = 0;
    public static final Integer CLASS_TOKEN_INDEX = 1;
    public static final Integer ENUM_TOKEN_INDEX = 2;
    public static final Integer VARIABLE_TOKEN_INDEX = 3;
    public static final Integer ENUM_MEMBER_TOKEN_INDEX = 4;
    public static final Integer FUNCTION_TOKEN_INDEX = 5;
    public static final Integer METHOD_TOKEN_INDEX = 6;
    public static final Integer KEYWORD_TOKEN_INDEX = 7;
}
