package requests;

import org.eclipse.lsp4j.Location;
import parser.SimpleNode;

public class Vertex {
    // по заданной локации ищет вершину в дереве
    static SimpleNode find(Location location, SimpleNode root) {
        if (root == null || location == null) {
            return null;
        }

        if (root.jjtGetNumChildren() == 0) {
            if (root.jjtGetFirstToken().beginLine <= location.getRange().getStart().getLine() &&
                root.jjtGetFirstToken().endLine >= location.getRange().getEnd().getLine() &&
                root.jjtGetFirstToken().beginColumn <= location.getRange().getStart().getCharacter() &&
                root.jjtGetFirstToken().endColumn >= location.getRange().getEnd().getCharacter()) {
                return root;
            }
        }
        else {
            for (int i = 0; i < root.jjtGetNumChildren(); i++) {
                SimpleNode vertex = (SimpleNode) root.jjtGetChild(i);
                SimpleNode res = find(location, vertex);
                if (res != null) {
                    return res;
                }
            }
        }
        return null;
    }

    public static boolean isStartsEarlier(SimpleNode first, SimpleNode second) {
        if (first.jjtGetFirstToken().beginLine < second.jjtGetFirstToken().beginLine) {
            return true;
        }
        if (first.jjtGetFirstToken().beginLine > second.jjtGetFirstToken().beginLine) {
            return false;
        }
        return first.jjtGetFirstToken().beginColumn <= second.jjtGetFirstToken().beginColumn;
    }
}
