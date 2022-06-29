package server;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import parser.Parser;
import parser.ParserConstants;
import parser.SimpleNode;
import parser.Token;
import requests.CompletionProvider;
import requests.DefinitionProvider;
import server.completion.patterns.HoverPatterns;
import server.semantic.tokens.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

import static parser.ParserTreeConstants.*;
import static parser.ParserTreeConstants.JJTCLASSDECLARATIONSTATEMENT;

public class ChapelTextDocumentService implements TextDocumentService {
    private final Logger LOG;
    private final DefinitionProvider definitionProvider;
    private CompletionProvider completionProvider;


    public ChapelTextDocumentService(Logger LOG, DefinitionProvider definitionProvider) {
        this.LOG = LOG;
        this.definitionProvider = definitionProvider;
    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        try {
            MarkupContent res = new MarkupContent();

            List<String> lines = new ArrayList<>();
            var doc = new File(new URI(params.getTextDocument().getUri()));
            Scanner readFile = new Scanner(doc);
            int line = params.getPosition().getLine();
            int character = params.getPosition().getCharacter();
            int currentLine = 0;
            while (readFile.hasNextLine() && currentLine < line) {
                currentLine++;
                lines.add(readFile.nextLine());
            }
            String cursorLine = "";
            if (readFile.hasNextLine()) {
                cursorLine = readFile.nextLine();
            }
            lines.add(cursorLine);
            if (character >= cursorLine.length()) {
                character = 0;
            }
            boolean isComment = false;
            for (int i = character; i > 0; i--) {
                if (cursorLine.toCharArray()[i] == '/' && cursorLine.toCharArray()[i - 1] == '/') {
                    isComment = true;
                    break;
                }
            }

            if (isComment) {
                res = new MarkupContent("markdown", "Comment section");
                readFile.close();
                return CompletableFuture.completedFuture(new Hover(res));
            }

            var cursorCharArray = cursorLine.toCharArray();
            StringBuilder actualLine = new StringBuilder();

            StringBuilder cursorWord = new StringBuilder();

            int wordBegin = character - 1;
            while (wordBegin < cursorCharArray.length && wordBegin > 0 && cursorCharArray[wordBegin - 1] != ' ') {
                wordBegin--;
            }

            while (wordBegin >= 0 && wordBegin < cursorCharArray.length && cursorCharArray[wordBegin] != ' ' && cursorCharArray[wordBegin] != ';') {
                cursorWord.append(cursorCharArray[wordBegin]);
                wordBegin++;
            }

            var hoverPattern = new HoverPatterns();

            if (!Objects.equals(hoverPattern.isDataStructure(cursorWord.toString()), "NONE")) {
                actualLine.append("Data Structure: [").append(cursorWord).append("](");
                actualLine.append(hoverPattern.isDataStructure(cursorWord.toString()));
                actualLine.append(")");
            } else if (!Objects.equals(hoverPattern.isLanguageSupport(cursorWord.toString()), "NONE")) {
                actualLine.append("Language support: [").append(cursorWord).append("](");
                actualLine.append(hoverPattern.isLanguageSupport(cursorWord.toString()));
                actualLine.append(")");
            } else if (!Objects.equals(hoverPattern.hasDocumentation(cursorWord.toString()), "NONE")) {
                actualLine.append(cursorWord).append(" [documentation](");
                actualLine.append(hoverPattern.hasDocumentation(cursorWord.toString()));
                actualLine.append(")");
            } else {
                actualLine.append(cursorWord);
            }

            boolean isVariable = false;
            res = new MarkupContent("markdown", actualLine.toString());

            readFile.close();
            return CompletableFuture.completedFuture(new Hover(res));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) throws RuntimeException {
        LOG.info("completion");
        var completionProvider = new CompletionProvider();
        SemanticTokens ans;
        try {
            var doc = new File(new URI(params.getTextDocument().getUri()));
            var rootNode = Parser.parse(doc.getAbsolutePath());
            assert rootNode != null;
            ans = findSemanticTokens(rootNode);
        } catch (Exception ignored) {
        }

        var res = completionProvider.getCompletion();
        return CompletableFuture.completedFuture(res);
    }

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
        return CompletableFuture.completedFuture(unresolved);
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
            return CompletableFuture.completedFuture(ans);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
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
//                Logger.getAnonymousLogger().info(v.module.name);
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
//            LOG.info("color: " + cc);
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
//            LOG.info(String.valueOf(cc));
//            LOG.info(importGroup.toString());
            for (var node : colorGroup) {
                node.module.usedModules.putAll(importGroup);
            }
        }
        LinkedList<ChapelStatement> statementsQueue = new LinkedList<>();
        statementsQueue.add(rootModule);
        while (!statementsQueue.isEmpty()) {
            var curStatement = statementsQueue.pollFirst();
            statementsQueue.addAll(curStatement.subStatements);
            statementsQueue.addAll(curStatement.procedures.values());
            statementsQueue.addAll(curStatement.subModules.values());
            for (var useStatement : curStatement.useStatements) {
                ArrayList<Map.Entry<String, ChapelModule>> modulesToUseList =
                        findModuleByUsePath(useStatement, useStatement.ownerModule, rootModule);
                if (modulesToUseList == null) {
                    continue;
                }
                for (var entry : modulesToUseList) {
                    curStatement.usedModules.putAll(entry.getValue().subModules);
                    curStatement.usedModules.putAll(entry.getValue().usedModules);
                    curStatement.usedModules.put(entry.getKey(), entry.getValue());
                }
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
                        moduleToUse = scopeModule.ownerModule;
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
//        LOG.info(fileModule.toString());

        ArrayList<SemanticToken> resTokens = new ArrayList<>();

        var queue = new LinkedList<ChapelModule>();
        queue.add(fileModule);
        ChapelModule currentModule;
        while (!queue.isEmpty()) {
            currentModule = queue.poll();
            inModule(currentModule, resTokens);
            queue.addAll(currentModule.subModules.values());
        }
        resTokens.sort((s1, s2) -> s1.line - s2.line == 0 ? s1.startChar - s2.startChar : s1.line - s2.line);
        var f = resTokens.stream().flatMap(x -> x.toArray().stream()).toList();
//        LOG.info("abs = " + f);
        for (int i = resTokens.size() - 1; i > 0; i--) {
            var curSemanticToken = resTokens.get(i);
            var prevSemanticToken = resTokens.get(i - 1);
            curSemanticToken.line -= prevSemanticToken.line;
            if (curSemanticToken.line == 0) {
                curSemanticToken.startChar -= prevSemanticToken.startChar;
            }
        }
        f = resTokens.stream().flatMap(x -> x.toArray().stream()).toList();
//        LOG.info("delta = " + f);
        return new SemanticTokens(f);
    }

    private void inModule(ChapelModule currentModule, ArrayList<SemanticToken> resTokens) {
        var currentReachableModules = new HashMap<String, ChapelModule>();
        currentReachableModules.putAll(currentModule.usedModules);
        currentReachableModules.putAll(currentModule.subModules);

        var currentReachableProcedures = new HashMap<>(currentModule.procedures);
        for (var module : currentReachableModules.values()) {
            currentReachableProcedures.putAll(module.procedures);
        }

        goThrough(currentModule, currentReachableModules, currentReachableProcedures, resTokens);
    }

    private void goThrough(ChapelStatement currentStatement,
                           HashMap<String, ChapelModule> reachableModules,
                           HashMap<String, ChapelProcedure> reachableProcedures,
                           ArrayList<SemanticToken> resTokens) {
        ArrayList<Map.Entry<String, ChapelModule>> addedModules = new ArrayList<>();
        for (var entry : currentStatement.usedModules.entrySet()) {
            if (!reachableModules.containsKey(entry.getKey())) {
                addedModules.add(entry);
                reachableModules.put(entry.getKey(), entry.getValue());
            }
        }

//        LOG.info(reachableModules.keySet().toString());
        ArrayList<Map.Entry<String, ChapelProcedure>> addedProcedures = new ArrayList<>();
        if (currentStatement.rootNode.getId() != JJTBLOCKSTATEMENT
                || !isBlockInClass(currentStatement.rootNode)
        ) {
            for (var entry : currentStatement.procedures.entrySet()) {
                if (!reachableProcedures.containsKey(entry.getKey())) {
//                    LOG.info("added proc: " + entry.getKey());
                    addedProcedures.add(entry);
                    reachableProcedures.put(entry.getKey(), entry.getValue());
                }
            }
        }

        for (var expr : currentStatement.expressions) {
            var firstToken = expr.rootNode.jjtGetFirstToken();
            var lastToken = expr.rootNode.jjtGetLastToken().next;

            var currentToken = firstToken;
            ArrayList<Token> idMemberTokens = new ArrayList<>();
//            LOG.info("last = " + lastToken.image);
            while (!currentToken.equals(lastToken)) {
                if (currentToken.kind == ParserConstants.ID) {
                    idMemberTokens.add(currentToken);
//                    LOG.info(currentToken.next.image);
                    if (currentToken.next.kind == ParserConstants.LPARENTHESIS) {
                        resTokens.addAll(
                                parseMembers(
                                        idMemberTokens,
                                        currentStatement,
                                        reachableModules,
                                        reachableProcedures,
                                        true));
                    } else if (currentToken.next.kind != ParserConstants.DOT) {
                        resTokens.addAll(
                                parseMembers(
                                        idMemberTokens,
                                        currentStatement,
                                        reachableModules,
                                        reachableProcedures,
                                        false));
                    }
                }
                currentToken = currentToken.next;
            }
        }
        for (ChapelStatement subStatement : currentStatement.subStatements) {
            goThrough(subStatement, reachableModules, reachableProcedures, resTokens);
        }
        for (ChapelStatement subStatement : currentStatement.procedures.values()) {
            goThrough(subStatement, reachableModules, reachableProcedures, resTokens);
        }

        for (var entry : addedModules) {
            reachableModules.remove(entry.getKey());
        }
        for (var entry : addedProcedures) {
            reachableProcedures.remove(entry.getKey());
        }
    }

    private boolean isBlockInClass(SimpleNode rootNode) {
        return ((SimpleNode) rootNode.jjtGetParent().jjtGetParent()).getId() == JJTCLASSDECLARATIONSTATEMENT;
    }

    private ArrayList<SemanticToken> parseMembers(ArrayList<Token> idMemberTokens,
                                                  ChapelStatement currentChapelExpressionStatement,
                                                  HashMap<String, ChapelModule> reachableModules,
                                                  HashMap<String, ChapelProcedure> reachableChapelProcedures,
                                                  boolean isCallable) {
        assert !idMemberTokens.isEmpty();
        var lastToken = idMemberTokens.get(idMemberTokens.size() - 1);
        var res = new ArrayList<SemanticToken>();

        BiFunction<Token, Integer, SemanticToken> createSemanticTokenFromId = (id, tokenNum) -> {
            return new SemanticToken(
                    id.beginLine - 1,
                    id.beginColumn - 1,
                    id.endColumn - (id.beginColumn - 1),
                    tokenNum,
                    0
            );
        };
        if (idMemberTokens.size() == 1 && isCallable) {
            if (reachableChapelProcedures.containsKey(lastToken.image)) {
                res.add(createSemanticTokenFromId.apply(lastToken, SemanticTokensConstants.FUNCTION_TOKEN_INDEX));
            } else {
                res.add(createSemanticTokenFromId.apply(lastToken, SemanticTokensConstants.METHOD_TOKEN_INDEX));
            }
            return res;
        }
        int modulesCallLength = idListCorrectModuleCallLength(idMemberTokens, reachableModules);
        for (int i = 0; i < modulesCallLength; i++) {
            var id = idMemberTokens.get(i);
            res.add(createSemanticTokenFromId.apply(id, SemanticTokensConstants.NAMESPACE_TOKEN_INDEX));
        }
        if (modulesCallLength == idMemberTokens.size() - 1 && isCallable) {
            res.add(createSemanticTokenFromId.apply(lastToken, SemanticTokensConstants.FUNCTION_TOKEN_INDEX));
            return res;
        }
        for (int i = modulesCallLength; i < idMemberTokens.size() - 1; i++) {
            var id = idMemberTokens.get(i);
            res.add(createSemanticTokenFromId.apply(id, SemanticTokensConstants.VARIABLE_TOKEN_INDEX));
        }
        if (isCallable) {
            res.add(createSemanticTokenFromId.apply(lastToken, SemanticTokensConstants.METHOD_TOKEN_INDEX));
        } else {
            res.add(createSemanticTokenFromId.apply(lastToken, SemanticTokensConstants.VARIABLE_TOKEN_INDEX));
        }
        return res;
    }

    private int idListCorrectModuleCallLength(ArrayList<Token> idList, HashMap<String, ChapelModule> reachableModules) {
        var it = reachableModules.get(idList.get(0).image);
        int i = 0;
        if (it == null) {
            return i;
        }
        i++;
//        LOG.info(it.name);
        for (; i < idList.size() - 1; i++) {
            var idToken = idList.get(i);
            var tokenImage = idToken.image;
            LOG.info(tokenImage);
            if (it.subModules.containsKey(tokenImage)) {
                it = it.subModules.get(tokenImage);
            } else if (it.usedModules.containsKey(tokenImage)) {
                it = it.usedModules.get(tokenImage);
            } else {
                break;
            }
        }
        return i;
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
                        determineParentModule(subModule, currentChapelStatement);

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
                        determineParentModule(procedure, currentChapelStatement);
                        currentChapelStatement.procedures.put(procedure.getName(), procedure);
                    }
                    case JJTUSESTATEMENT -> {
                        var useStatement = new ChapelUseStatement(currentContentNode);
                        determineParentModule(useStatement, currentChapelStatement);
                        currentChapelStatement.useStatements.add(useStatement);
                    }
                    case JJTIMPORTSTATEMENT -> {
                        // todo
                    }
                    case JJTVARIABLEDECLARATIONSTATEMENT -> {
                        idToken = getIdFromNode(currentContentNode);
                        assert idToken != null;
                        currentChapelStatement.variables.add(idToken.image);
                        currentChapelStatement.expressions.addAll(expressionsFromVarDeclaration(currentContentNode));

                    }
//                    case JJTCLASSDECLARATIONSTATEMENT -> {
//                        //todo
//                    }
                    default -> {
                        ChapelStatement childChapelStatement = new ChapelUnnamedStatement(currentContentNode);
                        childChapelStatement.ownerModule = currentChapelStatement.ownerModule;
                        determineParentModule(childChapelStatement, currentChapelStatement);
                        currentChapelStatement.subStatements.add(childChapelStatement);
                        queue.add(childChapelStatement);
                    }
                }
            }
        }
        return fileModule;
    }

    private List<ChapelExpression> expressionsFromVarDeclaration(SimpleNode currentContentNode) {
        if (currentContentNode.getId() == JJTEXPRESSION) {
            return List.of(new ChapelExpression(currentContentNode));
        }
        var ans = new ArrayList<ChapelExpression>();
        for (int i = 0; i < currentContentNode.jjtGetNumChildren(); i++) {
            var mbExprNode = (SimpleNode) currentContentNode.jjtGetChild(i);
            ans.addAll(expressionsFromVarDeclaration(mbExprNode));
        }
        return ans;
    }

    private void determineParentModule(ChapelStatement child, ChapelStatement current) {
        if (current.rootNode.getId() == JJTMODULEDECLARATIONSTATEMENT) {
            child.ownerModule = (ChapelModule) current;
        } else {
            child.ownerModule = current.ownerModule;
        }
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
                return "";
            } else if (part.toString().equals("InitializationPart")) {
                if (part.jjtGetChild(0).jjtGetChild(0).toString().equals("LiteralExpression")
                ) {
                    return part.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0).toString();
                }
            }
        }
        return "";
    }

    @Override
    public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
//        LOG.info(unresolved.toString());
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