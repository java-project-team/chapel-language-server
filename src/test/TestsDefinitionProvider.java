package requests;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import parser.Parser;
import parser.SimpleNode;
import java.net.URISyntaxException;
import java.util.Objects;

public class DefinitionProvider {
    public static void main(String[] args) throws URISyntaxException {
        SimpleNode root = Parser.parse("./src/test/resources/a.txt");
        {
            Location location = new Location("./src/test/resources/a.txt", new Range(new Position(2, 1), new Position(2, 1)));
            SimpleNode res = find(location, root);

            System.out.println("\nANSWER1");

            if (res == null) {
                System.out.println(":((((");
            } else {
                System.out.println(res.toString());
                System.out.println(res.jjtGetFirstToken().image + " " +
                        res.jjtGetFirstToken().beginLine + "-" +
                        res.jjtGetFirstToken().beginColumn + ", " +
                        res.jjtGetLastToken().endLine + "-" +
                        res.jjtGetLastToken().endColumn);
            }
        }

        {
            Location location = new Location("./src/test/resources/a.txt", new Range(new Position(7, 5), new Position(7, 5)));
            SimpleNode res = find(location, root);

            System.out.println("\nANSWER2");

            if (res == null) {
                System.out.println(":((((");
            } else {
                System.out.println(res.toString());
                System.out.println(res.jjtGetFirstToken().image + " " +
                        res.jjtGetFirstToken().beginLine + "-" +
                        res.jjtGetFirstToken().beginColumn + ", " +
                        res.jjtGetLastToken().endLine + "-" +
                        res.jjtGetLastToken().endColumn);
            }
        }

        {
            Location location = new Location("./src/test/resources/a.txt", new Range(new Position(10, 1), new Position(10, 1)));
            SimpleNode res = find(location, root);

            System.out.println("\nANSWER3");

            if (res == null) {
                System.out.println(":((((");
            } else {
                System.out.println(res.toString());
                System.out.println(res.jjtGetFirstToken().image + " " +
                        res.jjtGetFirstToken().beginLine + "-" +
                        res.jjtGetFirstToken().beginColumn + ", " +
                        res.jjtGetLastToken().endLine + "-" +
                        res.jjtGetLastToken().endColumn);
            }
        }

        {
            Location location = new Location("./src/test/resources/a.txt", new Range(new Position(13, 1), new Position(13, 1)));
            SimpleNode res = find(location, root);

            System.out.println("\nANSWER4");

            if (res == null) {
                System.out.println(":((((");
            } else {
                System.out.println(res.toString());
                System.out.println(res.jjtGetFirstToken().image + " " +
                        res.jjtGetFirstToken().beginLine + "-" +
                        res.jjtGetFirstToken().beginColumn + ", " +
                        res.jjtGetLastToken().endLine + "-" +
                        res.jjtGetLastToken().endColumn);
            }
        }

    }

    static SimpleNode find(Location location, SimpleNode root) {
        if (root == null) {
            root = Parser.parse(location.getUri());
        }
        SimpleNode vertex = Vertex.find(location, root);
        // TODO if (isVar)
        return findProviderVariable(vertex);
    }

    static boolean isVarDefinition(SimpleNode vertexVariable, SimpleNode vertex) {
        if (!Objects.equals(vertex.toString(), "VariableDeclarationStatement")) {
            return false;
        }

        for (int i = 0; i < vertex.jjtGetNumChildren(); i++) {
            if (Vertex.isStartsEarlier((SimpleNode) vertex.jjtGetChild(i), vertexVariable) &&
                    Objects.equals(vertex.jjtGetChild(i).toString(), "VariableDeclaration") &&
                    Objects.equals(((SimpleNode) vertex.jjtGetChild(i)).jjtGetFirstToken().image, vertexVariable.jjtGetFirstToken().image)) {
                return true;
            }
        }
        return false;
    }

    static SimpleNode findProviderVariable(SimpleNode vertex) {
        if (vertex == null) {
            System.out.println("Null in findProviderVariable");
            return null;
        }

        SimpleNode vertexVariable = vertex;
        while (vertex.jjtGetParent() != null) {
            vertex = (SimpleNode) vertex.jjtGetParent();
            if (Objects.equals(vertex.toString(), "Block") || Objects.equals(vertex.toString(), "BlockStatement")) {
                for (int i = 0; i < vertex.jjtGetNumChildren(); i++) {
                    for (int j = 0; j < vertex.jjtGetChild(i).jjtGetNumChildren(); j++) {
                        if (isVarDefinition(vertexVariable, (SimpleNode) vertex.jjtGetChild(i).jjtGetChild(j))) {
                            return (SimpleNode) vertex.jjtGetChild(i).jjtGetChild(j);
                        }
                    }
                }
            }
        }
        return null; // TODO if (null) {return findProviderVariableAnotherFile()}
    }
}