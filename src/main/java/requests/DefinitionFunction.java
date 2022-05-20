package requests;

import parser.SimpleNode;

import java.util.Objects;

// чет пока не очень поняла что да как

public class DefinitionFunction {
    private final String type;
    private final String name;

    public DefinitionFunction(SimpleNode function) {
        type = function.jjtGetFirstToken().image;
        name = function.jjtGetFirstToken().next.image;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
