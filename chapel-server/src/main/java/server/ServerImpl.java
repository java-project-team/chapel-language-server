package server;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.json.adapters.TypeUtils;
import org.eclipse.lsp4j.services.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static server.ServerConstants.InsertVarTypeCommand;

public class ServerImpl implements LanguageServer, LanguageClientAware {
    private static final Logger LOG = Logger.getLogger("server");
    private LanguageClient client = null;
    private Gson gson = new Gson();
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
        execOptions.setCommands(Collections.singletonList(InsertVarTypeCommand));
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
        return new TextDocumentService() {
            @Override
            public void didOpen(DidOpenTextDocumentParams params) {

            }

            @Override
            public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
                List<CodeLens> list = new ArrayList<>();

                CodeLens lens = new CodeLens();
                lens.setRange(new Range(new Position(0, 0), new Position(0, 10)));

                lens.setCommand(new Command(
                        "title",
                        InsertVarTypeCommand,
                        Collections.singletonList(params.getTextDocument().getUri())));
                list.add(lens);

                return CompletableFuture.completedFuture(list);
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
        };
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return new WorkspaceService() {
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
                var uri = ((JsonElement)params.getArguments().get(0)).getAsString();

                LOG.info(uri);
                // todo
                Map<String, List<TextEdit>> stringListMap = new HashMap<>();

                stringListMap.put(uri, Collections.singletonList(new TextEdit(
                        new Range(new Position(0, 0), new Position(0, 1)), "gotcha"
                )));
                workspaceEdit.setChanges(stringListMap);

                applyWorkspaceEditParams.setEdit(workspaceEdit);

                client.applyEdit(applyWorkspaceEditParams);
                switch (params.getCommand()){
                    case InsertVarTypeCommand:
                        break;
                    default:
                        LOG.info("unknown command: " + params.getCommand());
                }
                return CompletableFuture.completedFuture("");
            }
        };
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
    }
}
