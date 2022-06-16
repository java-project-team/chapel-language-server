package server.semantic.tokens;

import parser.SimpleNode;

public class ChapelProcedure extends ChapelNamedStatement {

    public ChapelProcedure(SimpleNode contentNode, String name) {
        super(contentNode, name);
    }

    public String getName() {
        return name;
    }
}
