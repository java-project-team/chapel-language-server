package requests;

import parser.SimpleNode;

// мб чего еще потребуется

public class DefinitionVariable {
    private final String name;

    public DefinitionVariable(SimpleNode variable) {
        name = variable.jjtGetFirstToken().image;
    }

    public DefinitionVariable(String variable) {
        name = variable;
    }

    public String getName() {
        return name;
    }
}
