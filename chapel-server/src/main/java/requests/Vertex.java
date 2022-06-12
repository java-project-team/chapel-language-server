package requests;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import parser.SimpleNode;

import java.util.logging.Logger;

public class Vertex {
    // по заданной локации ищет вершину в дереве
    static SimpleNode find(Logger LOG, Position position, SimpleNode root) {
        if (root == null || position == null) {
            return null;
        }

        if (root.jjtGetNumChildren() == 0) {
            if (root.jjtGetFirstToken().beginLine <= position.getLine() &&
                root.jjtGetFirstToken().endLine >= position.getLine() &&
                root.jjtGetFirstToken().beginColumn <= position.getCharacter() &&
                root.jjtGetFirstToken().endColumn >= position.getCharacter()) {
                return root;
            }
        }
        else {
            for (int i = 0; i < root.jjtGetNumChildren(); i++) {
                SimpleNode vertex = (SimpleNode) root.jjtGetChild(i);
                SimpleNode res = find(LOG, position, vertex);
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
