package requests;

import org.eclipse.lsp4j.Location;
import org.eclipse.xtext.xbase.lib.Pair;
import parser.Parser;
import parser.SimpleNode;

import java.util.*;

public class DefinitionProvider {
    private static BasicProcessing filesInformation;

    public DefinitionProvider(BasicProcessing filesInformation) {
        this.filesInformation = filesInformation;
    }

    public List<Pair<SimpleNode, String>> find(Location location, SimpleNode root) {
        System.out.println(location.getUri());
        filesInformation.addFile(location.getUri());

        if (root == null) {
            root = Parser.parse(location.getUri());
        }
        SimpleNode vertex = Vertex.find(location, root);

        if (vertex == null) {
            return null;
        }

        if (Objects.equals(vertex.jjtGetFirstToken().next.image, "(")) {
            return findProviderFunction(location.getUri(), vertex.jjtGetFirstToken().image);
        }
        else /*if ()*/ { // TODO здесь нужно проверить, не тип ли это
            List<Pair<SimpleNode, String>> ans = new ArrayList<>();
            var res = findProviderVariable(vertex);
            if (res != null) {
                ans.add(new Pair<>(res, location.getUri()));
            }
            return ans;
        }
    }

    public static boolean isVarDefinition(SimpleNode vertexVariable, SimpleNode vertex) {
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

    private static List<Pair<SimpleNode, String>> findProviderFunction(String file, String function) { // TODO тут по хорошему list надо бы
        List<Pair<SimpleNode, String>> ans = new ArrayList<>();
        {
            List<DefinitionFunction> functions = filesInformation.getFileInformation(file).getFunctions();
            for (DefinitionFunction i : functions) {
                if (Objects.equals(i.getName(), function)) {
                    ans.add(new Pair<>(i.getNode(), file));
                }
            }
            if (!ans.isEmpty()) {
                return ans;
            }
        }

        Set<String> namesFiles = filesInformation.getNamesFiles();
        for (String name : namesFiles) {
            List<DefinitionFunction> functions = filesInformation.getFileInformation(name).getFunctions();
            for (DefinitionFunction i : functions) {
                if (Objects.equals(i.getName(), function)) {
                    ans.add(new Pair<>(i.getNode(), name));
                }
            }
        }
        return ans;
    }

    private static SimpleNode findProviderVariable(SimpleNode vertex) {
        if (vertex == null) {
            //System.out.println("Null in findProviderVariable");
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