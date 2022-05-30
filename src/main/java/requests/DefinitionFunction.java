package requests;

import parser.SimpleNode;

import java.util.Objects;

// чет пока не очень поняла что да как

public class DefinitionFunction {
    private final String type;
    private final String name;
    private final SimpleNode node;

    public DefinitionFunction(SimpleNode function) {
        type = function.jjtGetFirstToken().image;
        name = function.jjtGetFirstToken().next.image;
        node = function;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public SimpleNode getNode() { return node; }
}
