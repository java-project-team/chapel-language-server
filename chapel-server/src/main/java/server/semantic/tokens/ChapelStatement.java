package server.semantic.tokens;

import parser.ParserConstants;
import parser.ParserTreeConstants;
import parser.SimpleNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

public class ChapelStatement {
    public boolean equals(ChapelStatement obj) {
        return this.rootNode.equals(obj.rootNode);
    }

    protected static void addAllStatements(Collection<SimpleNode> dest, SimpleNode root) {
        assert root != null;
        for (int i = 0; i < root.jjtGetNumChildren(); i++) {
            var child = (SimpleNode)root.jjtGetChild(i);
            if (child.getId() != ParserTreeConstants.JJTSTATEMENT) {
                continue;
            }
            dest.add(child);
        }
    }
    public ArrayList<SimpleNode> contentNodes = new ArrayList<>();
    public SimpleNode rootNode;
    public ChapelStatement parentStatement = null;

    public final HashMap<String, ChapelProcedure> procedures = new HashMap<>();
    public final ArrayList<ChapelStatement> subStatements = new ArrayList<>();
    public final HashSet<String> variables = new HashSet<>();

    public final HashMap<String, ChapelModule> modules = new HashMap<>();

//    protected ChapelStatement() {}
    protected ChapelStatement(SimpleNode newRootNode) {
        this.rootNode = newRootNode;
//        if (newRootNode.getId() == ParserTreeConstants.JJTFILE) {
//            this.contentNodes.add(newRootNode);
//            return;
//        }
//        for (int i = 0; i < newRootNode.jjtGetNumChildren(); i++) {
//            var child = (SimpleNode)newRootNode.jjtGetChild(i);
//            var childId = child.getId();
//            if (childId == ParserTreeConstants.JJTSTATEMENT) {
//                contentNodes.add(child);
//            }
//        }
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
