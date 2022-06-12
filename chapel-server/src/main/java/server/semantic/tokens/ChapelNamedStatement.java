package server.semantic.tokens;

import parser.SimpleNode;

import java.util.HashMap;
import java.util.HashSet;

public class ChapelNamedStatement extends ChapelStatement {
    public String name;



    public ChapelNamedStatement(SimpleNode contentNode, String name) {
        super(contentNode);
        this.name = name;
    }
}
