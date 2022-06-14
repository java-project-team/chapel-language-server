package server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;
import parser.*;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import requests.FileInformation;
import server.semantic.tokens.ChapelModule;
import server.semantic.tokens.ChapelProcedure;
import server.semantic.tokens.ChapelStatement;
import server.semantic.tokens.ChapelUnnamedStatement;

public class ServerImpl implements LanguageServer, LanguageClientAware {
    private static final Logger LOG = Logger.getLogger("server");
    private LanguageClient client = null;
    private final Gson gson = new Gson();
    private List<WorkspaceFolder> folders;
    private HashMap<String, FileInformation> fileInformationMap = new HashMap<>();

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setCodeActionProvider(false);
        capabilities.setColorProvider(false);
        capabilities.setDeclarationProvider(false);
        capabilities.setDefinitionProvider(false);
        capabilities.setHoverProvider(false);

        capabilities.setCallHierarchyProvider(true);

        folders = params.getWorkspaceFolders();
        // todo не работает
//        for (var folder : folders) {
//            try (var pathsStream = Files.walk(Path.of(new URI(folder.getUri())))) {
//                pathsStream.forEach(path -> {
//                    if (path.toFile().isFile()) {
//                        fileInformationMap.put(path.toUri().toString(),
//                                new FileInformation(path.toAbsolutePath().toString()));
//                    }
//                });
//            } catch (Exception e) {
//                LOG.warning(e.getLocalizedMessage());
//            }
//        }
//        for (var e : fileInformationMap.entrySet()) {
//            LOG.info(e.getValue().getFunctions().toString());
//        }

        var codeLensOptions = new CodeLensOptions();
        codeLensOptions.setWorkDoneProgress(false);
        codeLensOptions.setResolveProvider(true);
        capabilities.setCodeLensProvider(codeLensOptions);

        var execOptions = new ExecuteCommandOptions();
        execOptions.setCommands(Collections.singletonList(ServerConstants.InsertVarTypeCommand));
        capabilities.setExecuteCommandProvider(execOptions);

        var semanticTokensProvider = new SemanticTokensWithRegistrationOptions();
        semanticTokensProvider.setFull(true);
        semanticTokensProvider.setRange(false);
        semanticTokensProvider.setLegend(
                new SemanticTokensLegend(
                        List.of("class", "enum", "variable", "enumMember", "function", "method", "keyword"),
                        List.of()));
        capabilities.setSemanticTokensProvider(semanticTokensProvider);

        return CompletableFuture.completedFuture(new InitializeResult(capabilities));
    }

    @Override
    public CompletableFuture<Object> shutdown() {

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {
        System.exit(0);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return new ChapelTextDocumentService();
    }

    private class ChapelTextDocumentService implements TextDocumentService {

        @Override
        public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
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
            ChapelModule fileModule = createChapelModule(rootNode);
//            var ans = getTokensFromChapelStatement(fileModule);
            LOG.info(fileModule.toString());
            class SemanticTokenFinder {
                final HashMap<String, ChapelProcedure> availableProcedures = new HashMap<>();
                SemanticTokens generateTokens(ChapelStatement currentChapelStatement) {
                    for (ChapelStatement subStatement : currentChapelStatement.subStatements) {
                        if (subStatement.rootNode.getId() != ParserTreeConstants.JJTUSESTATEMENT) {
                            continue;
                        }

                    }
                    return null;
                }

                void resolveUseDependencies() {

                }
             }

//            var queue = new LinkedList<ChapelModule>();
//            queue.add(fileModule);
//            while (!queue.isEmpty()) {
//                var currentModule = queue.pollFirst();
//                queue.addAll(currentModule.modules.values());
//
//                SemanticTokens tokensFromModule = getTokensFromModule(currentModule);
//            }


            // бфсом идти по модулям
            // В модуле:
            //   составить иерархию импортов
            //   обойти бфсом исполняемые стейтменты для конкретного модуля

            return null;
        }

        private SemanticTokens getTokensFromChapelStatement(ChapelStatement currentModule) {

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
//                LOG.info(currentChapelStatement.rootNode.toString());
//                LOG.info(currentChapelStatement.contentNodes.toString());
                for (var currentContentNode : currentChapelStatement.contentNodes) {
                    assert currentContentNode != null;
                    if (currentContentNode.getId() != ParserTreeConstants.JJTSTATEMENT &&
                            currentContentNode.getId() != ParserTreeConstants.JJTENUMCONSTANT) {
                        assert false;
                        continue;
                    }
                    currentContentNode = (SimpleNode) currentContentNode.jjtGetChild(0);
                    assert currentContentNode != null;
                    Token idToken;
                    switch (currentContentNode.getId()) {
                        case ParserTreeConstants.JJTMODULEDECLARATIONSTATEMENT -> {
                            idToken = getIdFromNode(currentContentNode);
                            assert idToken != null;
                            ChapelModule subModule =
                                    new ChapelModule(
                                            currentContentNode,
                                            idToken.image);
                            subModule.parentStatement = currentChapelStatement;
                            currentChapelStatement.modules.put(subModule.name, subModule);
                            queue.add(subModule);
                        }
                        case ParserTreeConstants.JJTPROCEDUREDECLARATIONSTATEMENT -> {
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
                        case ParserTreeConstants.JJTUSESTATEMENT -> {
                        }
                        case ParserTreeConstants.JJTIMPORTSTATEMENT -> {

                        }
                        case ParserTreeConstants.JJTVARIABLEDECLARATIONSTATEMENT -> {
                            idToken = getIdFromNode(currentContentNode);
                            assert idToken != null;
                            currentChapelStatement.variables.add(idToken.image);
                        }
                        case ParserTreeConstants.JJTCLASSDECLARATIONSTATEMENT -> {
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

        }

        @Override
        public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
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

    @Override
    public WorkspaceService getWorkspaceService() {
        return new ChapelWorkspaceService();
    }

    private class ChapelWorkspaceService implements WorkspaceService {
        @Override
        public void didChangeConfiguration(DidChangeConfigurationParams params) {
        }

        @Override
        public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
            connect(null);
        }

        @Override
        public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
            ApplyWorkspaceEditParams applyWorkspaceEditParams = new ApplyWorkspaceEditParams();
            WorkspaceEdit workspaceEdit = new WorkspaceEdit();
            var uri = ((JsonElement) params.getArguments().get(0)).getAsString();

            LOG.info(uri);
            // todo
            Map<String, List<TextEdit>> stringListMap = new HashMap<>();

            stringListMap.put(uri, Collections.singletonList(new TextEdit(
                    new Range(new Position(0, 0), new Position(0, 1)), "gotcha"
            )));
            workspaceEdit.setChanges(stringListMap);

            applyWorkspaceEditParams.setEdit(workspaceEdit);

            client.applyEdit(applyWorkspaceEditParams);
            switch (params.getCommand()) {
                case ServerConstants.InsertVarTypeCommand:
                    break;
                default:
                    LOG.info("unknown command: " + params.getCommand());
            }
            return CompletableFuture.completedFuture("");
        }
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
    }
}
