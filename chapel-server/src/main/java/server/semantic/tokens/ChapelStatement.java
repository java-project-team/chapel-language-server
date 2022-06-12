package server.semantic.tokens;

import parser.ParserConstants;
import parser.ParserTreeConstants;
import parser.SimpleNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ChapelStatement {
    public boolean hasBlock = false;
    public SimpleNode contentNode;
    public SimpleNode rootNode;

    public final HashMap<String, ChapelProcedure> procedures = new HashMap<>();
    public final ArrayList<ChapelStatement> subStatements = new ArrayList<>();
    public final HashSet<String> variables = new HashSet<>();

    public final HashMap<String, ChapelModule> modules = new HashMap<>();

    public ChapelStatement(SimpleNode newRootNode) {
        this.rootNode = newRootNode;
        for (int i = newRootNode.jjtGetNumChildren() - 1; i >= 0 ; i--) {
            var child = (SimpleNode)newRootNode.jjtGetChild(i);
            var nodeId = child.getId();
            if (nodeId == ParserTreeConstants.JJTBLOCKSTATEMENT ||
                nodeId == ParserTreeConstants.JJTFUNCTIONBODY ||
                nodeId == ParserTreeConstants.JJTCLASSBODY ||
                nodeId == ParserTreeConstants.JJTENUMBODY ||
                nodeId == ParserTreeConstants.JJTRECORDBODY ||
                nodeId == ParserTreeConstants.JJTUNIONBODY) {
                this.hasBlock = true;
                this.contentNode = child;
                break;
            }
        }
    }
}
