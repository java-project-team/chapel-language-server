package server.semantic.tokens;

import parser.ParserConstants;
import parser.ParserTreeConstants;
import parser.SimpleNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

public class ChapelStatement {
    public ArrayList<SimpleNode> contentNodes = new ArrayList<>();
    public SimpleNode rootNode;

    public final HashMap<String, ChapelProcedure> procedures = new HashMap<>();
    public final ArrayList<ChapelStatement> subStatements = new ArrayList<>();
    public final HashSet<String> variables = new HashSet<>();

    public final HashMap<String, ChapelModule> modules = new HashMap<>();

    public ChapelStatement(SimpleNode newRootNode) {
        this.rootNode = newRootNode;
        if (newRootNode.getId() == ParserTreeConstants.JJTFILE) {
            this.contentNodes.add(newRootNode);
            return;
        }
        for (int i = 0; i < newRootNode.jjtGetNumChildren(); i++) {
            var child = (SimpleNode)newRootNode.jjtGetChild(i);
            var childId = child.getId();
            if (childId == ParserTreeConstants.JJTBLOCKSTATEMENT ||
                childId == ParserTreeConstants.JJTENUMBODY) {
                this.contentNodes.add(child);
            } else if (childId == ParserTreeConstants.JJTSTATEMENT &&
                    ((SimpleNode)child.jjtGetChild(0)).getId() == ) {

            }
        }
    }
    @Override
    public String toString() {
        return "\n{\n" + String.join(
                ", ",
                rootNode == null ? "null" : rootNode.toString(),
                "Modules = " + modules,
                "Procedures = " + procedures,
                "Variables = " + variables,
                "SubStatements = " + subStatements) + "\n}\n" ;
    }
}
