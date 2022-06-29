package requests;

import parser.SimpleNode;

import java.util.Objects;

public class DefinitionFunction {
    private final String type;
    private final String name;
    private final SimpleNode node;
    private final boolean isParentheses;

    public DefinitionFunction(SimpleNode function) {
        type = function.jjtGetFirstToken().image;
        name = function.jjtGetFirstToken().next.image;
        node = function;
        isParentheses = Objects.equals(function.jjtGetFirstToken().next.next.image, "(");
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean getIsParentheses() { return isParentheses; }

    public SimpleNode getNode() { return node; }
}
