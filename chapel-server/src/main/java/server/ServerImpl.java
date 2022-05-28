package server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;
import parser.Parser;
import parser.SimpleNode;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class ServerImpl implements LanguageServer, LanguageClientAware {
    private static final Logger LOG = Logger.getLogger("server");
    private LanguageClient client = null;
    private final Gson gson = new Gson();

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setCodeActionProvider(false);
        capabilities.setCallHierarchyProvider(false);
        capabilities.setColorProvider(false);
        capabilities.setDeclarationProvider(false);
        capabilities.setDefinitionProvider(false);
        capabilities.setHoverProvider(false);

        var codeLensOptions = new CodeLensOptions();
        codeLensOptions.setWorkDoneProgress(false);
        codeLensOptions.setResolveProvider(true);
        capabilities.setCodeLensProvider(codeLensOptions);

        var execOptions = new ExecuteCommandOptions();
        execOptions.setCommands(Collections.singletonList(ServerConstants.InsertVarTypeCommand));
        capabilities.setExecuteCommandProvider(execOptions);

//        var provider = new SemanticTokensWithRegistrationOptions();

//        capabilities.setSemanticTokensProvider();
        LOG.info("Initialized");
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
                LOG.info("1");
                if (part.toString().equals("TypePart")) {
                    LOG.info("2");
                    return "";
                } else if (part.toString().equals("InitializationPart")) {
                    if (part.jjtGetChild(0).jjtGetChild(0).toString().equals("LiteralExpression")
                    ) {
                        LOG.info("4");
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
