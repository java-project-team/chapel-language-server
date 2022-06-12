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
        DefinitionProvider.filesInformation = filesInformation;
    }

    public List<Location> findDumb(Location location) {
        filesInformation.addFile(location.getUri());
        SimpleNode root = filesInformation.getFileInformation(location.getUri()).getRoot();
        root.dump("");
        return null;
    }

    public List<Pair<String, SimpleNode>> findDeclarationNode(Logger LOG, String uri, Position position) {
        filesInformation.addFile(uri);
        SimpleNode root = filesInformation.getFileInformation(uri).getRoot();
        LOG.info(root.toString());

        SimpleNode vertex = Vertex.find(LOG, position, root);
        if (vertex == null) {
            return new ArrayList<>();
        }

        if (Objects.equals(vertex.jjtGetFirstToken().next.image, "(")) {
            LOG.info("find func def");
            return findDeclarationFunctionNode(LOG, uri, vertex.jjtGetFirstToken().image);
        }
        else { // TODO здесь нужно проверить, не тип ли это, или еще круче, найти определение типа!!!   !_! -_- -_- -_- -_- ~_~ ????????::::::!!!!!!!!????????????;;;; ((
            List<Pair<String, SimpleNode>> ans = new ArrayList<>();
            var res = findDeclarationVariableNode(LOG, vertex);
            if (res != null) {
                ans.add(new Pair<>(uri, res));
            }
            return ans;
        }
    }

    public List<Location> findDefinition(Logger LOG, String uri, Position position) {
        var ans = findDeclarationNode(LOG, uri, position);
        if (ans.isEmpty()) {
            return new ArrayList<>();
        }
        if (Objects.equals(ans.get(0).getValue().toString(), "ProcedureDeclarationStatement")){
            return ans.stream().map(res -> new Location(res.getKey(), new Range(new Position(res.getValue().jjtGetFirstToken().beginLine,
                    res.getValue().jjtGetFirstToken().beginColumn), new Position(res.getValue().jjtGetLastToken().endLine,
                    res.getValue().jjtGetLastToken().endColumn)))).toList();
        }

        var vertex = ans.get(0).getValue();
        if (Objects.equals(ans.get(0).getValue().toString(), "VariableDeclarationStatement")) {
            for (int i = 0; i < vertex.jjtGetNumChildren(); i++) {
                if(Objects.equals(vertex.jjtGetChild(i).toString(), "VariableDeclarationList")) {
                    for (int j = 0; j < vertex.jjtGetChild(i).jjtGetNumChildren(); j++) {
                        for (int k = 0; k < vertex.jjtGetChild(i).jjtGetChild(j).jjtGetNumChildren(); k++) {
                            if (Objects.equals(vertex.jjtGetChild(i).jjtGetChild(j).jjtGetChild(k).toString(), "InitializationPart")) {
                                return ans.stream().map(res -> new Location(res.getKey(), new Range(new Position(res.getValue().jjtGetFirstToken().beginLine,
                                        res.getValue().jjtGetFirstToken().beginColumn), new Position(res.getValue().jjtGetLastToken().endLine,
                                        res.getValue().jjtGetLastToken().endColumn)))).toList();
                            }
                        }
                    }
                }
            }
        }

        SimpleNode vertexDeclaration = vertex;
        vertex = (SimpleNode) vertex.jjtGetParent().jjtGetParent();
        for (int i = 0; i < vertex.jjtGetNumChildren(); i++) {
            if (Objects.equals(vertex.jjtGetChild(i).toString(), "Statement") // да не знаю, сделай dfs какой-нибудь, а я спать
                    && Objects.equals(((SimpleNode) vertex.jjtGetChild(i)).jjtGetFirstToken().image, vertex.jjtGetFirstToken().image)) {
            }
        }

        /*while (vertex.jjtGetParent() != null) {
            vertex = (SimpleNode) vertex.jjtGetParent();
            if (Objects.equals(vertex.toString(), "File") || Objects.equals(vertex.toString(), "Block") || Objects.equals(vertex.toString(), "BlockStatement") || Objects.equals(vertex.toString(), "Statement")) {
                for (int i = 0; i < vertex.jjtGetNumChildren(); i++) {
                    for (int j = 0; j < vertex.jjtGetChild(i).jjtGetNumChildren(); j++) {
                        if (isVarDefinition(LOG, vertexDeclaration, (SimpleNode) vertex.jjtGetChild(i).jjtGetChild(j))) {
                            return (SimpleNode) vertex.jjtGetChild(i).jjtGetChild(j);
                        }
                    }
                }
            }
        }*/
        return null;
    }

    public List<Location> findDeclaration(Logger LOG, String uri, Position position) {
        var ans = findDeclarationNode(LOG, uri, position);
        return ans.stream().map(res -> new Location(res.getKey(), new Range(new Position(res.getValue().jjtGetFirstToken().beginLine,
                res.getValue().jjtGetFirstToken().beginColumn), new Position(res.getValue().jjtGetLastToken().endLine,
                res.getValue().jjtGetLastToken().endColumn)))).toList();
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

    private static List<Pair<String, SimpleNode>> findDeclarationFunctionNode(Logger LOG, String file, String function) {
        List<Pair<String, SimpleNode>> ans = new ArrayList<>();
        {
            List<DefinitionFunction> functions = filesInformation.getFileInformation(file).getFunctions();
            LOG.info(functions.toString());
            for (DefinitionFunction i : functions) {
                if (Objects.equals(i.getName(), function)) {
                    ans.add(new Pair<>(file, i.getNode()));
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
                    ans.add(new Pair<>(name, i.getNode()));
                }
            }
        }
        return ans;
    }

    private static SimpleNode findDeclarationVariableNode(Logger LOG, SimpleNode vertex) {
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

        return null; // TODO if (null) {return findProviderVariableNodeAnotherFile()}
    }
}