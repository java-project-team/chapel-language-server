package server.semantic.tokens;

import parser.ParserTreeConstants;
import parser.SimpleNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

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
    public ArrayList<ChapelUseStatement> useStatements = new ArrayList<>();
    public SimpleNode rootNode;
    public ChapelModule ownerModule = null;

    public final HashMap<String, ChapelProcedure> procedures = new HashMap<>();
    public final ArrayList<ChapelStatement> subStatements = new ArrayList<>();
    public final HashSet<String> variables = new HashSet<>();

    public final HashMap<String, ChapelModule> subModules = new HashMap<>();
    public final HashMap<String, ChapelModule> usedModules = new HashMap<>();

    protected ChapelStatement(SimpleNode newRootNode) {
        this.rootNode = newRootNode;
    }
    @Override
    public String toString() {
        return "\n{\n" + String.join(
                ", ",
                rootNode == null ? "null" : rootNode.toString(),
                "Modules = " + subModules,
                "Uses = " + useStatements,
                "Procedures = " + procedures,
                "Variables = " + variables,
                "SubStatements = " + subStatements) + "\n}\n" ;
    }
}
