package server.semantic.tokens;

import parser.ParserConstants;
import parser.ParserTreeConstants;
import parser.SimpleNode;

import java.util.HashMap;
import java.util.HashSet;

public class ChapelNamedStatement extends ChapelStatement {
    public String name;



    public ChapelNamedStatement(SimpleNode rootNode, String name) {
        super(rootNode);
        this.name = name;
        for (int i = 0; i < rootNode.jjtGetNumChildren(); i++) {
            var child = (SimpleNode)rootNode.jjtGetChild(i);
            if (child.getId() == ParserTreeConstants.JJTBLOCKSTATEMENT) {
                addAllStatements(contentNodes, child);
            } else {
                contentNodes.add(child);
            }
        }
    }
}
