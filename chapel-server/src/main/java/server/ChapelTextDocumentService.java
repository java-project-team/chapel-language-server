package server;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import parser.Parser;
import parser.ParserTreeConstants;
import parser.ParserConstants;
import parser.SimpleNode;
import parser.Token;
import requests.DefinitionProvider;
import server.semantic.tokens.*;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

import static parser.ParserTreeConstants.*;
import static parser.ParserTreeConstants.JJTCLASSDECLARATIONSTATEMENT;

public class ChapelTextDocumentService implements TextDocumentService {
    private final Logger LOG;
    private final DefinitionProvider definitionProvider;

    public ChapelTextDocumentService(Logger LOG, DefinitionProvider definitionProvider) {
        this.LOG = LOG;
        this.definitionProvider = definitionProvider;
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(DefinitionParams params) {
        LOG.info("definition");
        params.getPosition().setLine(params.getPosition().getLine() + 1);
        params.getPosition().setCharacter(params.getPosition().getCharacter() + 1);
        try {
            var res = definitionProvider.findDefinition(LOG, (new URI(params.getTextDocument().getUri())).getPath(), params.getPosition());
            if (res == null) {
                res = new ArrayList<>();
            }
            res = res.stream().peek(a -> {
                a.getRange().getStart().setLine(a.getRange().getStart().getLine() - 1);
                a.getRange().getEnd().setLine(a.getRange().getEnd().getLine() - 1);
                a.getRange().getStart().setCharacter(a.getRange().getStart().getCharacter() - 1);
                a.getRange().getEnd().setCharacter(a.getRange().getEnd().getCharacter() - 1);
            }).toList();
//            LOG.info(res.toString());
            return CompletableFuture.completedFuture(Either.forLeft(res));
        } catch (Exception ignored) {
            return null;
        }
    }

    // у меня в стандартных не было, добавляла сама (Ctrl + Shift + A)
    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> declaration(DeclarationParams params) {
        LOG.info("declaration");
        params.getPosition().setLine(params.getPosition().getLine() + 1);
        params.getPosition().setCharacter(params.getPosition().getCharacter() + 1);
        try {
            var res = definitionProvider.findDeclaration(LOG, (new URI(params.getTextDocument().getUri())).getPath(), params.getPosition());
            if (res == null) {
                res = new ArrayList<>();
            }
            res = res.stream().peek(a -> {
                a.getRange().getStart().setLine(a.getRange().getStart().getLine() - 1);
                a.getRange().getEnd().setLine(a.getRange().getEnd().getLine() - 1);
                a.getRange().getStart().setCharacter(a.getRange().getStart().getCharacter() - 1);
                a.getRange().getEnd().setCharacter(a.getRange().getEnd().getCharacter() - 1);
            }).toList();
//            LOG.info(res.toString());
            return CompletableFuture.completedFuture(Either.forLeft(res));
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
        LOG.info("semanticTokensFull");
        try {
            var doc = new File(new URI(params.getTextDocument().getUri()));
            var rootNode = Parser.parse(doc.getAbsolutePath());
            assert rootNode != null;
            SemanticTokens ans = findSemanticTokens(rootNode);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(new SemanticTokens(new ArrayList<>()));
    }

    void importHierarchy(ChapelModule rootModule) {
        class DFSNode {
            static class Edge {
                final DFSNode dest;
                final boolean isPublic;

                public Edge(DFSNode dest, boolean isPublic) {
                    this.dest = dest;
                    this.isPublic = isPublic;
                }
            }
            int index = 0;
            public final ChapelModule module;
            public ArrayList<Edge> to = new ArrayList<>();
            public ArrayList<Edge> from = new ArrayList<>();
            static final ArrayList<DFSNode> allDFSNodes = new ArrayList<>();
            DFSNode(ChapelModule module) {
                this.module = module;
                index = allDFSNodes.size();
                allDFSNodes.add(this);
            }
            static void reachDFS(int[] colors, Integer color, DFSNode v) {
                if (colors[v.index] != 0) {
                    return;
                }
                Logger.getAnonymousLogger().info(v.module.name);
                colors[v.index] = color;
                for (var from : v.from) {
                    if (!from.isPublic) {
                        continue;
                    }
                    reachDFS(colors, color, from.dest);
                }
            }

            static void topSort(boolean[] visited, LinkedList<DFSNode> order, DFSNode v) {
                if (visited[v.index]) {
                    return;
                }
                visited[v.index] = true;
                for (var to : v.to) {
                    if (!to.isPublic) {
                        continue;
                    }
                    topSort(visited, order, to.dest);
                }
                order.addFirst(v);
            }
        }
        DFSNode.allDFSNodes.clear();

        LinkedList<ChapelModule> queue = new LinkedList<>();
        HashMap<ChapelModule, DFSNode> mapFromModuleToNode = new HashMap<>();
        queue.add(rootModule);
        while (!queue.isEmpty()) {
            var module = queue.pollFirst();
            var node = new DFSNode(module);
            mapFromModuleToNode.put(module, node);
            queue.addAll(module.subModules.values());
        }

        for (var dfsNode : DFSNode.allDFSNodes) {
            if (dfsNode.module.useStatements.isEmpty()) {
                continue;
            }
            for (
                    int i = 0;
                    i < dfsNode.module.useStatements.size();
                    i++) {
                var useStatement = dfsNode.module.useStatements.get(i);
                ArrayList<Map.Entry<String, ChapelModule>> modulesToUseList =
                        findModuleByUsePath(useStatement, dfsNode.module, rootModule);
                if (modulesToUseList == null) {
                    continue;
                }
                for (var moduleToUse : modulesToUseList) {
                    var fromNode = mapFromModuleToNode.get(moduleToUse.getValue());
                    assert fromNode != null;
                    dfsNode.module.usedModules.putAll(fromNode.module.subModules);
                    dfsNode.module.usedModules.put(moduleToUse.getKey(), moduleToUse.getValue());
                    DFSNode.Edge from = new DFSNode.Edge(fromNode, useStatement.isPublic);
                    DFSNode.Edge to = new DFSNode.Edge(dfsNode, useStatement.isPublic);
                    dfsNode.from.add(from);
                    fromNode.to.add(to);
                }
            }
        }
        boolean[] isVisited = new boolean[DFSNode.allDFSNodes.size()];

        LinkedList<DFSNode> topSortOrder = new LinkedList<>();
        for (var dfsNode : DFSNode.allDFSNodes) {
            DFSNode.topSort(isVisited, topSortOrder, dfsNode);
        }

        int[] color = new int[DFSNode.allDFSNodes.size()];
        int cc = 0;
        for (var dfsNode : topSortOrder) {
            cc++;
            LOG.info("color: " + cc);
            DFSNode.reachDFS(color, cc, dfsNode);
        }

        ArrayList<ArrayList<DFSNode>> colorsGroups = new ArrayList<>();
        for (int i = 0; i < cc; i++) {
            colorsGroups.add(new ArrayList<>());
        }

        for (int i = 0; i < DFSNode.allDFSNodes.size(); i++) {
            var dfsNode = DFSNode.allDFSNodes.get(i);
            int c = color[i];
            colorsGroups.get(c - 1).add(dfsNode);
        }
        cc = 0;
        for (var colorGroup : colorsGroups) {
            HashMap<String, ChapelModule> importGroup = new HashMap<>();
            for (var node : colorGroup) {
                importGroup.putAll(node.module.subModules);
                importGroup.putAll(node.module.usedModules);
            }
            cc++;
            LOG.info(String.valueOf(cc));
            LOG.info(importGroup.toString());
            for (var node : colorGroup) {
                node.module.usedModules.putAll(importGroup);
            }
        }
    }

    private ArrayList<Map.Entry<String, ChapelModule>> findModuleByUsePath(ChapelUseStatement useStatement,
                                                                           ChapelModule useOwnerModule,
                                                                           ChapelModule rootModule) {
        ArrayList<Map.Entry<String, ChapelModule>> ans = new ArrayList<>();
        for (var useModuleDec : useStatement.useModules) {
            Function<ChapelModule, ChapelModule> tryToFindModule = (scopeModule) -> {
                for (var moduleName : useModuleDec.modules) {
                    if (scopeModule == null) {
                        return null;
                    }
                    var moduleToUse = scopeModule.subModules.get(moduleName);
                    if (moduleToUse == null) {
                        moduleToUse = scopeModule.usedModules.get(moduleName);
                    }
                    if (moduleName.equals("this")) {
                        moduleToUse = scopeModule;
                    } else if (moduleName.equals("super")) {
                        moduleToUse = (ChapelModule) scopeModule.parentStatement;
                    }
                    scopeModule = moduleToUse;
                }
                return scopeModule;
            };

            var target = tryToFindModule.apply(useOwnerModule);
            if (target == null) {
                if (useModuleDec.modules.get(0).equals("this") || useModuleDec.modules.get(0).equals("super")) {
                    return null;
                }
                target = tryToFindModule.apply(rootModule);
                if (target == null) {
                    return null;
                }
            }
            ans.add(new AbstractMap.SimpleEntry<>(useModuleDec.name, target));
        }
        return ans;
    }

    private SemanticTokens findSemanticTokens(SimpleNode rootNode) {

//        LOG.info(dump(rootNode, ""));
        ChapelModule fileModule = createChapelModule(rootNode);
        importHierarchy(fileModule);
        ArrayList<Integer> resTokens = new ArrayList<>();


//        LOG.info(fileModule.toString());
        var queue = new LinkedList<ChapelStatement>();
        queue.add(fileModule);
        ChapelStatement currentChapelStatement = null;

        while (!queue.isEmpty()) {
            currentChapelStatement = queue.poll();

            switch (currentChapelStatement.rootNode.getId()) {
                case JJTEXPRESSION -> {
                    var firstToken = currentChapelStatement.rootNode.jjtGetFirstToken();
                    var lastToken = currentChapelStatement.rootNode.jjtGetLastToken();

                    var currentToken = firstToken;
                    ArrayList<Token> idMemberTokens = new ArrayList<>();
                    while (!currentToken.equals(lastToken)) {
                        if (currentToken.kind == ParserConstants.ID) {
                            idMemberTokens.add(currentToken);
                            if (currentToken.next.kind == ParserConstants.LPARENTHESIS) {
                                // todo добавить токен об определении функция ли это или метод
                            }
                        }
                        currentToken = currentToken.next;
                    }
                }
                default -> {

                }
            }

            queue.addAll(currentChapelStatement.subStatements);
        }

        return null;
    }

    private String dump(SimpleNode rootNode, String prefix) {
        StringBuilder ans = new StringBuilder(prefix + rootNode.toString());
        for (int i = 0; i < rootNode.jjtGetNumChildren(); ++i) {
            SimpleNode child = (SimpleNode) rootNode.jjtGetChild(i);
            if (child != null) {
                ans.append("\n").append(dump(child, prefix + "  "));
            }
        }
        return ans.toString();
    }

    private ChapelModule createChapelModule(SimpleNode rootNode) {
        var fileModule = new ChapelModule(rootNode, "");
        var queue = new LinkedList<ChapelStatement>();
        queue.add(fileModule);
        ChapelStatement currentChapelStatement;
        while (!queue.isEmpty()) {
            currentChapelStatement = queue.poll();
            for (var currentContentNode : currentChapelStatement.contentNodes) {
                assert currentContentNode != null;
                if (currentContentNode.getId() != JJTSTATEMENT &&
                        currentContentNode.getId() != JJTENUMCONSTANT) {
                    assert false;
                    continue;
                }
                currentContentNode = (SimpleNode) currentContentNode.jjtGetChild(0);
                assert currentContentNode != null;
                Token idToken;
                switch (currentContentNode.getId()) {
                    case JJTMODULEDECLARATIONSTATEMENT -> {
                        idToken = getIdFromNode(currentContentNode);
                        assert idToken != null;
                        ChapelModule subModule =
                                new ChapelModule(
                                        currentContentNode,
                                        idToken.image);
                        subModule.parentStatement = currentChapelStatement;
                        currentChapelStatement.subModules.put(subModule.name, subModule);
                        queue.add(subModule);
                    }
                    case JJTPROCEDUREDECLARATIONSTATEMENT -> {
                        if (!checkIsProcedure(currentContentNode)) {
                            continue;
                        }
                        idToken = getIdFromNode(currentContentNode);
                        assert idToken != null;
                        ChapelProcedure procedure = new ChapelProcedure(currentContentNode, idToken.image);
                        queue.add(procedure);
                        procedure.parentStatement = currentChapelStatement.parentStatement;
                        currentChapelStatement.procedures.put(procedure.getName(), procedure);
                    }
                    case JJTUSESTATEMENT -> {
                        var useStatement = new ChapelUseStatement(currentContentNode);
                        useStatement.parentStatement = currentChapelStatement;
                        currentChapelStatement.useStatements.add(useStatement);
                    }
                    case JJTIMPORTSTATEMENT -> {

                    }
                    case JJTVARIABLEDECLARATIONSTATEMENT -> {
                        idToken = getIdFromNode(currentContentNode);
                        assert idToken != null;
                        currentChapelStatement.variables.add(idToken.image);
                    }
                    case JJTCLASSDECLARATIONSTATEMENT -> {
                    }
                    default -> {
                        ChapelStatement chapelStatement = new ChapelUnnamedStatement(currentContentNode);
                        chapelStatement.parentStatement = currentChapelStatement.parentStatement;
                        currentChapelStatement.subStatements.add(chapelStatement);
                        queue.add(chapelStatement);
                    }
                }
            }
        }
        return fileModule;
    }

    private boolean checkIsProcedure(SimpleNode currentNode) {
        for (Token currentToken = currentNode.jjtGetFirstToken();
             !currentToken.equals(currentNode.jjtGetLastToken());
             currentToken = currentToken.next) {
            if (currentToken.kind == ParserConstants.PROC || currentToken.kind == ParserConstants.OPERATOR) {
                return currentToken.kind == ParserConstants.PROC;
            }
        }
        assert false;
        return false;
    }

    private Token getIdFromNode(SimpleNode node) {
        for (Token currentToken = node.jjtGetFirstToken();
             !currentToken.equals(node.jjtGetLastToken());
             currentToken
                     = currentToken.next) {
            if (currentToken.kind == ParserConstants.ID) {
                return currentToken;
            }
        }
        return null;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        LOG.info("didOpen");
    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
        LOG.info("codeLens");
        List<CodeLens> list = new ArrayList<>(getVarTypeLensesInDocument(params.getTextDocument()));
        return CompletableFuture.completedFuture(list);
    }

    List<CodeLens> getVarTypeLensesInDocument(TextDocumentIdentifier docId) {
        var ans = new ArrayList<CodeLens>();
        try {
            var node = Parser.parse(new URI(docId.getUri()).getPath());
            assert node != null;
            var x = getVarTypeLensesInNode(node);
            ans.addAll(x);
        } catch (Exception e) {
//                client.logMessage(new MessageParams(MessageType.Warning, e.getMessage()));
        }
        return ans;
    }

    List<CodeLens> getVarTypeLensesInNode(SimpleNode node) {
        var ans = new ArrayList<CodeLens>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            var child = (SimpleNode) node.jjtGetChild(i);
            switch (child.toString()) {
                case "ExpressionStatement":
                    break;
                // var x = 10;
                case "VariableDeclarationStatement":
                    for (int j = 0; j < child.jjtGetNumChildren(); j++) {
                        var notTerm = (SimpleNode) child.jjtGetChild(j);
                        if (notTerm.jjtGetFirstToken().image.equals("var")) {
                            ans.addAll(resolveTitles(child));
                        }
                    }
                    break;
                default:
                    ans.addAll(getVarTypeLensesInNode(child));
                    break;
            }

        }
        return ans;
    }

    private List<CodeLens> resolveTitles(SimpleNode varDec) {
        List<CodeLens> list = new ArrayList<>();
        for (int i = 0; i < varDec.jjtGetNumChildren(); i++) {
            var child = (SimpleNode) varDec.jjtGetChild(i);
            if (child.toString().equals("VariableDeclaration")) {
                var start = new Position(
                        child.jjtGetFirstToken().beginLine - 1,
                        child.jjtGetFirstToken().beginColumn);
                var end = new Position(
                        child.jjtGetFirstToken().beginLine - 1,
                        child.jjtGetFirstToken().endColumn
                );
                var lens = new CodeLens();
                lens.setRange(new Range(start, end));
                var command = new Command();
                command.setTitle(
                        getTitleForPrimitive(child));
                lens.setCommand(command);
                list.add(lens);
            }
        }
        return list;
    }

    private String getTitleForPrimitive(SimpleNode child) {
        for (int i = 1; i < child.jjtGetNumChildren(); i++) {
            var part = child.jjtGetChild(i);
            if (part.toString().equals("TypePart")) {
//                    LOG.info("2");
                return "";
            } else if (part.toString().equals("InitializationPart")) {
                if (part.jjtGetChild(0).jjtGetChild(0).toString().equals("LiteralExpression")
                ) {
//                        LOG.info("4");
                    return part.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0).toString();
                }
            }
        }
        return "";
    }

    @Override
    public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
        LOG.info(unresolved.toString());
        return CompletableFuture.completedFuture(unresolved);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {

    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {

    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {

    }

    @Override
    public CompletableFuture<List<CallHierarchyItem>> prepareCallHierarchy(CallHierarchyPrepareParams params) {

        return null;
    }
}