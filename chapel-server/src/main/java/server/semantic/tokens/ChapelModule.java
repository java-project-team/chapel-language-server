package server.semantic.tokens;

import parser.SimpleNode;

import java.util.HashMap;
import java.util.HashSet;

public class ChapelModule extends ChapelNamedStatement {

    public ChapelModule(SimpleNode contentNode, String name) {
        super(contentNode, name);
    }

    @Override
    public String toString() {
        return String.join(
                "\n",
                contentNode.toString(),
                modules.toString(),
                procedures.toString(),
                variables.toString());
    }
}
