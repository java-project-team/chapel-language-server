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

    public List<Location> findDeclarationInModule(Logger LOG, String uri, SimpleNode var, List<String> modules) {
        //filesInformation.addFile(uri);
        //SimpleNode root = filesInformation.getFileInformation(uri).getRoot();
        //var vertexWithModule = new Pair<>(var, modules);
        LOG.info("FFF 41");
        var trueModules = Vertex.findModule(LOG, var).getValue();
        trueModules.addAll(modules);
        LOG.info("FFF 43");
        modules = trueModules;
        LOG.info("modules: " + modules.toString());
        var module = filesInformation.getFileInformation(uri);
        for (var i = 0; i < modules.size(); i++) {
            module = module.getUseModules().get(modules.get(i));
        }

        List<SimpleNode> declarations;
        LOG.info("mb type: " + var.toString() + "  " + var.jjtGetFirstToken().image + " " + (module == null));
        if (Objects.equals(var.jjtGetFirstToken().next.image, "(")) {
            declarations = module
                    .getFunctions()
                    .stream()
                    .filter((a) -> {
                        LOG.info("FFF");
                        return Objects.equals(a.getName(), var.jjtGetFirstToken().image);
                    })
                    .map(DefinitionFunction::getNode)
                    .toList();
        } else {
            declarations = module
                    .getVariables()
                    .stream()
                    .filter((a) -> Objects.equals(a.getName(), var.jjtGetFirstToken().image))
                    .map(DefinitionVariable::getNode)
                    .toList();
        }
        LOG.info("FFF 4");
        SimpleNode ans = null;
        for (var i : declarations) {
            if (Vertex.isStartsEarlier(i, var) && (ans == null || Vertex.isStartsEarlier(ans, i))) {
                ans = i;
            }
        }
        if (ans == null) {
            return new ArrayList<>();
        }
        return List.of(new Location(uri, new Range(new Position(ans.jjtGetFirstToken().beginLine,
                ans.jjtGetFirstToken().beginColumn), new Position(ans.jjtGetLastToken().endLine,
                ans.jjtGetLastToken().endColumn))));
    }

    public List<Location> findDeclaration(Logger LOG, String uri, Position position) {
        filesInformation.addFile(uri);
        SimpleNode root = filesInformation.getFileInformation(uri).getRoot();
        SimpleNode vertex = Vertex.find(LOG, position, root);
        List<String> modules = new ArrayList<>();
        if (vertex != null) {
            for (int i = 0; i <= vertex.jjtGetParent().jjtGetNumChildren() && Objects.equals((vertex.jjtGetParent().jjtGetChild(i)).toString(), "Identifier") && vertex != vertex.jjtGetParent().jjtGetChild(i); i++) {
                modules.add(((SimpleNode) vertex.jjtGetParent().jjtGetChild(i)).jjtGetFirstToken().image);
            }
        }
        if (!modules.isEmpty() || (vertex != null && Objects.equals(vertex.jjtGetFirstToken().next.image, "("))) {
            return findDeclarationInModule(LOG, uri, vertex, modules);
        }
        var ans = findDeclarationNode(LOG, uri, position);
        return ans.listDeclaration.stream().map(res -> new Location(res.getKey(), new Range(new Position(res.getValue().jjtGetFirstToken().beginLine,
                res.getValue().jjtGetFirstToken().beginColumn), new Position(res.getValue().jjtGetLastToken().endLine,
                res.getValue().jjtGetLastToken().endColumn)))).toList();
    }


    ///////////////////////////////////////////////////////////////////
    private class ReturnFindDeclarationNode {
        public static class Const {
            final static int FUNCTION_DECLARATION = 1;
            final static int NOTHING_DECLARATION = 2;
            final static int VARIABLE_DECLARATION = 3;
            final static int TYPE_DECLARATION = 4;
        }

        public int type;
        public List<Pair<String, SimpleNode>> listDeclaration;
        public String name;

        ReturnFindDeclarationNode(int type, List<Pair<String, SimpleNode>> listDeclaration, String name) {
            this.type = type;
            this.listDeclaration = listDeclaration;
            this.name = name;
        }
    }

    public ReturnFindDeclarationNode findDeclarationNode(Logger LOG, String uri, Position position) {
        filesInformation.addFile(uri);
        SimpleNode root = filesInformation.getFileInformation(uri).getRoot();
        SimpleNode vertex = Vertex.find(LOG, position, root);
        if (vertex == null) {
            return new ReturnFindDeclarationNode(ReturnFindDeclarationNode.Const.NOTHING_DECLARATION, new ArrayList<>(), "");
        }

        if (Objects.equals(vertex.jjtGetFirstToken().next.image, "(")) {
            return new ReturnFindDeclarationNode(ReturnFindDeclarationNode.Const.FUNCTION_DECLARATION, findDeclarationFunctionNode(LOG, uri, vertex.jjtGetFirstToken().image), vertex.jjtGetFirstToken().image);
        } else { // TODO здесь нужно проверить, не тип ли это, или еще круче, найти определение типа!!!   !_! -_- -_- -_- -_- ~_~ ????????::::::!!!!!!!!????????????;;;; ((
            List<Pair<String, SimpleNode>> ans = new ArrayList<>();
            var res = findDeclarationVariableNode(LOG, vertex);
            if (res != null) {
                ans.add(new Pair<>(uri, res));
            }
            return new ReturnFindDeclarationNode(ReturnFindDeclarationNode.Const.VARIABLE_DECLARATION, ans, vertex.jjtGetFirstToken().image);
        }
    }

    public List<Location> findDefinition(Logger LOG, String uri, Position position) {
        var ans = findDeclarationNode(LOG, uri, position);
        if (ans.listDeclaration.isEmpty()) {
            return new ArrayList<>();
        }
        if (ans.type == ReturnFindDeclarationNode.Const.FUNCTION_DECLARATION) {
            return ans.listDeclaration.stream().map(res -> new Location(res.getKey(), new Range(new Position(res.getValue().jjtGetFirstToken().beginLine,
                    res.getValue().jjtGetFirstToken().beginColumn), new Position(res.getValue().jjtGetLastToken().endLine,
                    res.getValue().jjtGetLastToken().endColumn)))).toList();
        }

        var vertex = ans.listDeclaration.get(0).getValue();
        if (ans.type == ReturnFindDeclarationNode.Const.VARIABLE_DECLARATION) {
            for (int i = 0; i < vertex.jjtGetNumChildren(); i++) {
                if (Objects.equals(vertex.jjtGetChild(i).toString(), "VariableDeclarationList")) {
                    for (int j = 0; j < vertex.jjtGetChild(i).jjtGetNumChildren(); j++) {
                        for (int k = 0; k < vertex.jjtGetChild(i).jjtGetChild(j).jjtGetNumChildren(); k++) {
                            if (Objects.equals(vertex.jjtGetChild(i).jjtGetChild(j).jjtGetChild(k).toString(), "InitializationPart")) {
                                return ans.listDeclaration.stream().map(res -> new Location(res.getKey(), new Range(new Position(res.getValue().jjtGetFirstToken().beginLine,
                                        res.getValue().jjtGetFirstToken().beginColumn), new Position(res.getValue().jjtGetLastToken().endLine,
                                        res.getValue().jjtGetLastToken().endColumn)))).toList();
                            }
                        }
                    }
                }
            }
        }

        var res = dfs((SimpleNode) vertex.jjtGetParent().jjtGetParent(), vertex, ans.name);
        if (res == null) {
            return new ArrayList<>();
        }
        return List.of(new Location(uri, new Range(new Position(res.jjtGetFirstToken().beginLine,
                res.jjtGetFirstToken().beginColumn), new Position(res.jjtGetLastToken().endLine,
                res.jjtGetLastToken().endColumn))));
    }

    private SimpleNode dfs(SimpleNode root, SimpleNode vertexDeclaration, String name) {
        if (!Objects.equals(root.toString(), "ModuleDeclarationStatement")) {
            for (int i = 0; i < root.jjtGetNumChildren(); i++) {
                if (i + 1 < root.jjtGetNumChildren()
                        && Objects.equals(root.jjtGetChild(i).toString(), "Identifier")
                        && Objects.equals(((SimpleNode) root.jjtGetChild(i)).jjtGetFirstToken().image, name)
                        && Objects.equals(root.jjtGetChild(i + 1).toString(), "AssignOperators")
                        && Objects.equals(findDeclarationVariableNode(null, (SimpleNode) root.jjtGetChild(i)), vertexDeclaration)) {
                    return (SimpleNode) root.jjtGetParent();
                }
                var res = dfs((SimpleNode) root.jjtGetChild(i), vertexDeclaration, name);
                if (res != null) {
                    return res;
                }
            }
        }
        return null;
    }

    /*public List<Location> findDeclaration(Logger LOG, String uri, Position position) {
        filesInformation.addFile(uri);
        SimpleNode root = filesInformation.getFileInformation(uri).getRoot();
        var vertexWithModule = Vertex.findModule(LOG, position, root);
        var module = filesInformation.getFileInformation(uri);
        for (var i = vertexWithModule.getValue().size() - 1; i >= 0; i--) {
            module = module.getUseModules().get(vertexWithModule.getValue().get(i));
            LOG.info("MMM 1 " + module.getNameModule());
        }

        LOG.info("MMM 3 " + module.getVariables().size());
        var declarations = module
                .getVariables()
                .stream()
                .filter((a) -> Objects.equals(a.getName(), vertexWithModule.getKey().jjtGetFirstToken().image))
                .map(DefinitionVariable::getNode)
                .toList();
        LOG.info("MMM 3 " + declarations.size());
        SimpleNode ans = null;
        for (var i : declarations) {
            if (Vertex.isStartsEarlier(i, vertexWithModule.getKey()) && (ans == null || Vertex.isStartsEarlier(ans, i))) {
                ans = i;
            }
        }
        if (ans == null) {
            return new ArrayList<>();
        }
        return List.of(new Location(uri, new Range(new Position(ans.jjtGetFirstToken().beginLine,
                ans.jjtGetFirstToken().beginColumn), new Position(ans.jjtGetLastToken().endLine,
                ans.jjtGetLastToken().endColumn))));
    }*/

    private static boolean isVarDeclaration(Logger LOG, SimpleNode vertexVariable, SimpleNode vertex) {
        if (!Objects.equals(vertex.toString(), "VariableDeclarationStatement")) {
            return false;
        }

        for (int i = 0; i < vertex.jjtGetNumChildren(); i++) {
            if (Objects.equals(vertex.jjtGetChild(i).toString(), "VariableDeclarationList")) {
                for (int j = 0; j < vertex.jjtGetChild(i).jjtGetNumChildren(); j++) {
                    if (Objects.equals(vertex.jjtGetChild(i).jjtGetChild(j).toString(), "VariableDeclaration") &&
                            Vertex.isStartsEarlier((SimpleNode) vertex.jjtGetChild(i).jjtGetChild(j), vertexVariable)) {
                        if (Objects.equals(vertex.jjtGetChild(i).jjtGetChild(j).jjtGetChild(0).toString(), "IdentifierList")) {
                            for (int k = 0; k < vertex.jjtGetChild(i).jjtGetChild(j).jjtGetChild(0).jjtGetNumChildren(); k++) {
                                if (Objects.equals(((SimpleNode) vertex.jjtGetChild(i).jjtGetChild(j).jjtGetChild(0).jjtGetChild(k)).jjtGetFirstToken().image, vertexVariable.jjtGetFirstToken().image)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
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
        return ans; // TODO if empty {return findProviderFunctionNodeAnotherFile()}
    }

    private static SimpleNode findDeclarationVariableNode(Logger LOG, SimpleNode vertex) {
        if (vertex == null) {
            return null;
        }

        SimpleNode vertexVariable = vertex;
        SimpleNode res = null;
        while (vertex.jjtGetParent() != null && !Objects.equals(vertex.toString(), "ModuleDeclarationStatement")) {
            vertex = (SimpleNode) vertex.jjtGetParent();
            if (Objects.equals(vertex.toString(), "File")
                    || Objects.equals(vertex.toString(), "Block")
                    || Objects.equals(vertex.toString(), "BlockStatement")
                    || Objects.equals(vertex.toString(), "Statement")) {
                for (int i = 0; i < vertex.jjtGetNumChildren(); i++) {
                    for (int j = 0; j < vertex.jjtGetChild(i).jjtGetNumChildren(); j++) {
                        if (isVarDeclaration(LOG, vertexVariable, (SimpleNode) vertex.jjtGetChild(i).jjtGetChild(j))
                                && (res == null || Vertex.isStartsEarlier(res, (SimpleNode) vertex.jjtGetChild(i).jjtGetChild(j)))) {
                            res = (SimpleNode) vertex.jjtGetChild(i).jjtGetChild(j);
                        }
                    }
                }
            }
        }

        return res;
    }
}