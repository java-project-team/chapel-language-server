package requests;

import org.eclipse.lsp4j.Position;
import org.eclipse.xtext.xbase.lib.Pair;
import parser.SimpleNode;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Vertex {
    public static Pair<SimpleNode, List<String>> findModule(Logger LOG, Position position, SimpleNode root) {
        var vertex = find(LOG, position, root);
        return findModule(LOG, vertex);
    }
    public static Pair<SimpleNode, List<String>> findModule(Logger LOG, SimpleNode vertex) {
        List<String> ans = new ArrayList<>();
        for (var node = vertex; node != null; node = (SimpleNode) node.jjtGetParent()) {
            if (Objects.equals(node.toString(), "ModuleDeclarationStatement")) {
                String name = node.jjtGetFirstToken().next.image;
                if (name == "module") {
                    name = node.jjtGetFirstToken().next.next.image;
                }
                ans.add(name);
            }
        }
        Collections.reverse(ans);
        return new Pair<>(vertex, ans);
    }

    // по заданной локации ищет вершину в дереве
    static SimpleNode find(Logger LOG, Position position, SimpleNode root) {
        if (root == null || position == null) {
            return null;
        }

        if (root.jjtGetNumChildren() == 0) {
            if (root.jjtGetFirstToken().beginLine <= position.getLine() &&
                root.jjtGetLastToken().endLine >= position.getLine() &&
                root.jjtGetFirstToken().beginColumn <= position.getCharacter() &&
                root.jjtGetLastToken().endColumn + 1 >= position.getCharacter()) {
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
