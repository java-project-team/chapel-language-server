package server.semantic.tokens;

import parser.ParserConstants;
import parser.ParserTreeConstants;
import parser.SimpleNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

public class ChapelNamedStatement extends ChapelStatement {
    public String name;



    public ChapelNamedStatement(SimpleNode rootNode, String name) {
        super(rootNode);
        this.name = name;
        for (int i = 0; i < rootNode.jjtGetNumChildren(); i++) {
            var child = (SimpleNode)rootNode.jjtGetChild(i);
//            Logger.getAnonymousLogger().info(name);
//            Logger.getAnonymousLogger().info("au : " + child.toString());
            if (child.getId() != ParserTreeConstants.JJTSTATEMENT) {
                continue;
            }
            child = (SimpleNode) child.jjtGetChild(0);

            if (child.getId() == ParserTreeConstants.JJTBLOCKSTATEMENT) {
                addAllStatements(contentNodes, child);
            } else {
                contentNodes.add((SimpleNode) child.jjtGetParent());
            }
        }
    }
}
