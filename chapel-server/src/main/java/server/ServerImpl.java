package server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.*;
import parser.*;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

import requests.BasicProcessing;
import requests.DefinitionProvider;
import parser.Token;
import requests.FileInformation;
import server.semantic.tokens.*;

import static parser.ParserTreeConstants.*;
import static server.semantic.tokens.SemanticTokensConstants.LEGEND_TOKENS;

public class ServerImpl implements LanguageServer, LanguageClientAware {
    private static final Logger LOG = Logger.getLogger("server");
    private LanguageClient client = null;
    private final Gson gson = new Gson();
    private List<WorkspaceFolder> folders;
    //private HashMap<String, FileInformation> fileInformationMap = new HashMap<>();
    BasicProcessing basicProcessing = new BasicProcessing(new ArrayList<>());
    private DefinitionProvider definitionProvider = new DefinitionProvider(basicProcessing);

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setCodeActionProvider(false);
        capabilities.setColorProvider(false);
//        capabilities.setDeclarationProvider(true);
//        capabilities.setDefinitionProvider(true);
//        capabilities.setTypeDefinitionProvider(false); // чет не поняла, что это, и для этого даже клавиш нет
//        capabilities.setHoverProvider(false);

//        capabilities.setCallHierarchyProvider(true);

//        folders = params.getWorkspaceFolders();
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

//        var codeLensOptions = new CodeLensOptions();
//        codeLensOptions.setWorkDoneProgress(false);
//        codeLensOptions.setResolveProvider(true);
//        capabilities.setCodeLensProvider(codeLensOptions);

//        var execOptions = new ExecuteCommandOptions();
//        execOptions.setCommands(Collections.singletonList(ServerConstants.InsertVarTypeCommand));
//        capabilities.setExecuteCommandProvider(execOptions);

        var semanticTokensProvider = new SemanticTokensWithRegistrationOptions();
        semanticTokensProvider.setFull(true);
        semanticTokensProvider.setRange(false);
        semanticTokensProvider.setLegend(
                new SemanticTokensLegend(
                        LEGEND_TOKENS,
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
        return new ChapelTextDocumentService(LOG, definitionProvider);
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
