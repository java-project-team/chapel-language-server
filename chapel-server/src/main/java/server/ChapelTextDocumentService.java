package server;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import parser.Parser;
import parser.ParserConstants;
import parser.SimpleNode;
import parser.Token;
import requests.DefinitionProvider;
import server.semantic.tokens.ChapelModule;
import server.semantic.tokens.ChapelProcedure;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class ChapelTextDocumentService implements TextDocumentService {
    private final Logger LOG;
    private DefinitionProvider definitionProvider;

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
            LOG.info(res.toString());
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
            LOG.info(res.toString());
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
            SemanticTokens ans = findSemanticTokens(rootNode);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(new SemanticTokens(new ArrayList<>()));
    }

    private SemanticTokens findSemanticTokens(SimpleNode rootNode) {
        LOG.info(dump(rootNode, ""));
        ChapelModule currentFile = createChapelModule(rootNode);
        LOG.info(currentFile.toString());
        // бфсом идти по модулям
        // В модуле:
        //   составить иерархию импортов
        //   обойти бфсом исполняемые стейтменты для конкретного модуля

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
        var queue = new LinkedList<ChapelModule>();
        queue.add(fileModule);
        ChapelModule currentModule;
        SimpleNode currentNode;
        while (!queue.isEmpty()) {
            currentModule = queue.poll();
            currentNode = currentModule.getContentNode();
            int numChildren = currentNode.jjtGetNumChildren();
            for (int i = 0; i < numChildren; i++) {
                var statement = (SimpleNode) currentNode.jjtGetChild(i);
                assert statement != null;
                if (!statement.toString().equals("Statement")) {
                    continue;
                }
                statement = (SimpleNode) statement.jjtGetChild(0);
                assert statement != null;
//                    LOG.info(statement.toString());
                Token idToken;
                switch (statement.toString()) {
                    case "ModuleDeclarationStatement" -> {
                        idToken = getIdFromNode(statement);
                        assert idToken != null;
                        ChapelModule subModule =
                                new ChapelModule(
                                        // take a block
                                        (SimpleNode) statement.jjtGetChild(statement.jjtGetNumChildren() - 1),
                                        idToken.image);
                        currentModule.getModules().put(subModule.getName(), subModule);
                        queue.add(subModule);
                    }
                    case "ProcedureDeclarationStatement" -> {
                        if (!checkIsProcedure(currentNode)) {
                            continue;
                        }
                        idToken = getIdFromNode(statement);
                        assert idToken != null;
                        ChapelProcedure procedure = new ChapelProcedure(idToken.image);
                        currentModule.getProcedures().put(procedure.getName(), procedure);
                    }
                    case "VariableDeclarationStatement" -> {
                        idToken = getIdFromNode(currentNode);
                        assert idToken != null;
                        currentModule.getVariables().add(idToken.image);
                    }
                    default -> {
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