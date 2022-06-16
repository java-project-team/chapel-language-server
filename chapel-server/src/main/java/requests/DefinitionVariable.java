package requests;

import parser.SimpleNode;

// мб чего еще потребуется

public class DefinitionVariable {
    private final String name;
    private final SimpleNode nodeDeclaration;

    public DefinitionVariable(SimpleNode variable) {
        name = variable.jjtGetFirstToken().image;
        nodeDeclaration = variable;
    }

    public SimpleNode getNode() {
        return nodeDeclaration;
    }

    public String getName() {
        return name;
    }
}
