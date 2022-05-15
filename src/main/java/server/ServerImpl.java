package server;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.concurrent.CompletableFuture;

public class ServerImpl implements LanguageServer {
    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setCodeActionProvider(false);
        capabilities.setCallHierarchyProvider(false);
        capabilities.setColorProvider(false);
        capabilities.setDeclarationProvider(false);
        capabilities.setDefinitionProvider(false);
        capabilities.setHoverProvider(false);
//        var provider = new SemanticTokensWithRegistrationOptions();

//        capabilities.setSemanticTokensProvider();

//        var serverLauncher =
//                LSPLauncher.createServerLauncher(
//                        ServiceEndpoints.toServiceObject(, LanguageServer.class),
//                        System.in,
//                        System.out);
        return null;
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
        return null;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return null;
    }

}
