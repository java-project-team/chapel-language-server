package server.semantic.tokens;

import parser.ParserTreeConstants;
import parser.SimpleNode;

public class ChapelUnnamedStatement extends ChapelStatement {
    public ChapelUnnamedStatement(SimpleNode newRootNode) {
        super(newRootNode);
        for (int i = 0; i < newRootNode.jjtGetNumChildren(); i++) {
            var child = (SimpleNode) newRootNode.jjtGetChild(i);
            var childId = child.getId();
            if (childId == ParserTreeConstants.JJTSTATEMENT) {
                contentNodes.add(child);
            }
        }
    }
}
