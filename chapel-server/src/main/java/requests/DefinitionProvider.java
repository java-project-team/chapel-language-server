package requests;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.xtext.xbase.lib.Pair;
import parser.Parser;
import parser.SimpleNode;

import java.util.*;
import java.util.logging.Logger;

public class DefinitionProvider {
    private static BasicProcessing filesInformation;

    public DefinitionProvider(BasicProcessing filesInformation) {
        this.filesInformation = filesInformation;
    }

    public List<Location> findDumb(Location location) {
        filesInformation.addFile(location.getUri());
        SimpleNode root = filesInformation.getFileInformation(location.getUri()).getRoot();
        root.dump("");
        return null;
    }

    public List<Location> find(Logger LOG, String uri, Position position) {
        filesInformation.addFile(uri);
        SimpleNode root = filesInformation.getFileInformation(uri).getRoot();

        SimpleNode vertex = Vertex.find(LOG, position, root);
        if (vertex == null) {
            return new ArrayList<>();
        }

        if (Objects.equals(vertex.jjtGetFirstToken().next.image, "(")) {
            LOG.info("find func def");
            return findProviderFunction(LOG, uri, vertex.jjtGetFirstToken().image);
        }
        else { // TODO здесь нужно проверить, не тип ли это
            List<Location> ans = new ArrayList<>();
            var res = findProviderVariable(LOG, vertex);
            if (res != null) {
                ans.add(new Location(uri, new Range(new Position(res.jjtGetFirstToken().beginLine,
                        res.jjtGetFirstToken().beginColumn), new Position(res.jjtGetLastToken().endLine,
                        res.jjtGetLastToken().endColumn))));
            }
            return ans;
        }
    }

    private static boolean isVarDefinition(Logger LOG, SimpleNode vertexVariable, SimpleNode vertex) {
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

    private static List<Location> findProviderFunction(Logger LOG, String file, String function) {
        List<Pair<SimpleNode, String>> ans = new ArrayList<>();
        {
            List<DefinitionFunction> functions = filesInformation.getFileInformation(file).getFunctions();
            LOG.info(functions.toString());
            for (DefinitionFunction i : functions) {
                if (Objects.equals(i.getName(), function)) {
                    ans.add(new Pair<>(i.getNode(), file));
                }
            }
            if (!ans.isEmpty()) {
                return ans.stream().map(res -> new Location(res.getValue(), new Range(new Position(res.getKey().jjtGetFirstToken().beginLine,
                        res.getKey().jjtGetFirstToken().beginColumn), new Position(res.getKey().jjtGetLastToken().endLine,
                        res.getKey().jjtGetLastToken().endColumn)))).toList();
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
        return ans.stream().map(res -> new Location(res.getValue(), new Range(new Position(res.getKey().jjtGetFirstToken().beginLine,
                res.getKey().jjtGetFirstToken().beginColumn), new Position(res.getKey().jjtGetLastToken().endLine,
                res.getKey().jjtGetLastToken().endColumn)))).toList();
    }

    private static SimpleNode findProviderVariable(Logger LOG, SimpleNode vertex) {
        if (vertex == null) {
            return null;
        }

        SimpleNode vertexVariable = vertex;
        while (vertex.jjtGetParent() != null) {
            vertex = (SimpleNode) vertex.jjtGetParent();
            if (Objects.equals(vertex.toString(), "File") || Objects.equals(vertex.toString(), "Block") || Objects.equals(vertex.toString(), "BlockStatement") || Objects.equals(vertex.toString(), "Statement")) {
                for (int i = 0; i < vertex.jjtGetNumChildren(); i++) {
                    for (int j = 0; j < vertex.jjtGetChild(i).jjtGetNumChildren(); j++) {
                        if (isVarDefinition(LOG, vertexVariable, (SimpleNode) vertex.jjtGetChild(i).jjtGetChild(j))) {
                            return (SimpleNode) vertex.jjtGetChild(i).jjtGetChild(j);
                        }
                    }
                }
            }
        }

        return null; // TODO if (null) {return findProviderVariableAnotherFile()}
    }
}