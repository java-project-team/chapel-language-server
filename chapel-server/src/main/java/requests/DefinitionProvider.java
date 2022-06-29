package requests;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.xtext.xbase.lib.Pair;
import parser.SimpleNode;

import java.util.*;

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

    public List<Location> findDeclarationInModule(String uri, SimpleNode var, List<String> modules) {
        var parentModules = Vertex.findModule(var).getValue();
        List<Location> ans;

        try {
            ans = findDeclarationInLocalModule(uri, var, new ArrayList<>(), modules);
            if (!ans.isEmpty()) {
                return ans;
            }
        } catch (Exception ignore) {
        }


        try {
            ans = findDeclarationInLocalModule(uri, var, parentModules, modules);
            if (!ans.isEmpty()) {
                return ans;
            }
        } catch (Exception ignore) {
        }


        return new ArrayList<>();
    }

    public List<Location> findDeclarationInLocalModule(String uri, SimpleNode var, List<String> parentModules, List<String> modules) throws Exception {
        var module = filesInformation.getFileInformation(uri);
        for (String s : parentModules) {
            module = module.getUseModules().get(s);
        }
        for (String s : modules) {
            module = module.getUseModules().get(s);
            if (!module.isPublic) {
                throw new Exception();
            }
        }

        List<SimpleNode> declarations = new ArrayList<>();
        declarations.addAll(module
                .getFunctions()
                .stream()
                .filter((a) -> Objects.equals(a.getName(), var.jjtGetFirstToken().image)
                        && Objects.equals(a.getIsParentheses(), (Objects.equals(var.jjtGetFirstToken().next.image, "("))))
                .map(DefinitionFunction::getNode)
                .toList());
        declarations.addAll(module
                .getVariables()
                .stream()
                .filter((a) -> Objects.equals(a.getName(), var.jjtGetFirstToken().image))
                .map(DefinitionVariable::getNode)
                .toList());

        var answer = new ArrayList<Location>();
        for (var i : declarations) {
            answer.addAll(List.of(new Location(uri, new Range(new Position(i.jjtGetFirstToken().beginLine,
                    i.jjtGetFirstToken().beginColumn), new Position(i.jjtGetLastToken().endLine,
                    i.jjtGetLastToken().endColumn)))));
        }
        return answer;
    }

    public List<Location> findDeclaration(String uri, Position position) {
        filesInformation.addFile(uri);
        SimpleNode root = filesInformation.getFileInformation(uri).getRoot();
        SimpleNode vertex = Vertex.find(position, root);
        List<String> modules = new ArrayList<>();
        if (vertex != null) {
            for (int i = 0; i <= vertex.jjtGetParent().jjtGetNumChildren() && Objects.equals((vertex.jjtGetParent().jjtGetChild(i)).toString(), "Identifier") && vertex != vertex.jjtGetParent().jjtGetChild(i); i++) {
                String name = ((SimpleNode) vertex.jjtGetParent().jjtGetChild(i)).jjtGetFirstToken().image;
                if (Objects.equals(name, "module")) {
                    name = ((SimpleNode) vertex.jjtGetParent().jjtGetChild(i)).jjtGetFirstToken().next.image;
                }
                modules.add(name);
            }
        }
        if (!modules.isEmpty() || (vertex != null && Objects.equals(vertex.jjtGetFirstToken().next.image, "("))) {
            return findDeclarationInModule(uri, vertex, modules);
        }
        var ans = findDeclarationNode(uri, position);
        if (ans.listDeclaration.isEmpty()) {
            return findDeclarationInModule(uri, vertex, modules);
        }
        return ans.listDeclaration.stream().map(res -> new Location(res.getKey(), new Range(new Position(res.getValue().jjtGetFirstToken().beginLine,
                res.getValue().jjtGetFirstToken().beginColumn), new Position(res.getValue().jjtGetLastToken().endLine,
                res.getValue().jjtGetLastToken().endColumn)))).toList();
    }

    private static class ReturnFindDeclarationNode {
        public static class Const {
            final static int FUNCTION_DECLARATION = 1;
            final static int NOTHING_DECLARATION = 2;
            final static int VARIABLE_DECLARATION = 3;
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

    public ReturnFindDeclarationNode findDeclarationNode(String uri, Position position) {
        filesInformation.addFile(uri);
        SimpleNode root = filesInformation.getFileInformation(uri).getRoot();
        SimpleNode vertex = Vertex.find(position, root);
        if (vertex == null) {
            return new ReturnFindDeclarationNode(ReturnFindDeclarationNode.Const.NOTHING_DECLARATION, new ArrayList<>(), "");
        }

        if (Objects.equals(vertex.jjtGetFirstToken().next.image, "(")) {
            return new ReturnFindDeclarationNode(ReturnFindDeclarationNode.Const.FUNCTION_DECLARATION, findDeclarationFunctionNode(uri, vertex.jjtGetFirstToken().image, true), vertex.jjtGetFirstToken().image);
        } else {
            List<Pair<String, SimpleNode>> ans = new ArrayList<>(findDeclarationFunctionNode(uri, vertex.jjtGetFirstToken().image, false));
            var res = findDeclarationVariableNode(vertex);
            if (res != null) {
                ans.add(new Pair<>(uri, res));
            }
            return new ReturnFindDeclarationNode(ReturnFindDeclarationNode.Const.VARIABLE_DECLARATION, ans, vertex.jjtGetFirstToken().image);
        }
    }

    public List<Location> findDefinition(String uri, Position position) {
        var ans = findDeclarationNode(uri, position);
        if (ans.listDeclaration.isEmpty()) {
            return findDeclaration(uri, position);
        }

        List<Location> answer = new ArrayList<>(ans.listDeclaration.stream().map(res -> new Location(res.getKey(), new Range(new Position(res.getValue().jjtGetFirstToken().beginLine,
                res.getValue().jjtGetFirstToken().beginColumn), new Position(res.getValue().jjtGetLastToken().endLine,
                res.getValue().jjtGetLastToken().endColumn)))).toList());

        if (ans.type == ReturnFindDeclarationNode.Const.FUNCTION_DECLARATION) {
            return answer;
        }

        var vertex = ans.listDeclaration.get(0).getValue();
        if (ans.type == ReturnFindDeclarationNode.Const.VARIABLE_DECLARATION) {
            for (int i = 0; i < vertex.jjtGetNumChildren(); i++) {
                if (Objects.equals(vertex.jjtGetChild(i).toString(), "VariableDeclarationList")) {
                    for (int j = 0; j < vertex.jjtGetChild(i).jjtGetNumChildren(); j++) {
                        for (int k = 0; k < vertex.jjtGetChild(i).jjtGetChild(j).jjtGetNumChildren(); k++) {
                            if (Objects.equals(vertex.jjtGetChild(i).jjtGetChild(j).jjtGetChild(k).toString(), "InitializationPart")) {
                                answer.addAll(ans.listDeclaration.stream().map(res -> new Location(res.getKey(), new Range(new Position(res.getValue().jjtGetFirstToken().beginLine,
                                        res.getValue().jjtGetFirstToken().beginColumn), new Position(res.getValue().jjtGetLastToken().endLine,
                                        res.getValue().jjtGetLastToken().endColumn)))).toList());
                            }
                        }
                    }
                }
            }
        }

        var res = dfs((SimpleNode) vertex.jjtGetParent().jjtGetParent(), vertex, ans.name);
        if (res == null || !answer.isEmpty()) {
            if (answer.isEmpty()) {
                return findDeclaration(uri, position);
            }
            return answer;
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
                        && Objects.equals(findDeclarationVariableNode((SimpleNode) root.jjtGetChild(i)), vertexDeclaration)) {
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

    private static boolean isVarDeclaration(SimpleNode vertexVariable, SimpleNode vertex) {
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

    private static List<Pair<String, SimpleNode>> findDeclarationFunctionNode(String file, String function, boolean isParentheses) {
        List<Pair<String, SimpleNode>> ans = new ArrayList<>();
        {
            List<DefinitionFunction> functions = filesInformation.getFileInformation(file).getFunctions();
            for (DefinitionFunction i : functions) {
                if (Objects.equals(i.getName(), function) && i.getIsParentheses() == isParentheses) {
                    ans.add(new Pair<>(file, i.getNode()));
                }
            }
            return ans;
        }
    }

    private static SimpleNode findDeclarationVariableNode(SimpleNode vertex) {
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
                        if (isVarDeclaration(vertexVariable, (SimpleNode) vertex.jjtGetChild(i).jjtGetChild(j))
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