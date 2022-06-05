import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import parser.Parser;
import parser.SimpleNode;
import server.ServerImpl;

import java.io.*;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        ServerImpl server = new ServerImpl();
        Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, System.in, System.out);

        LanguageClient client = launcher.getRemoteProxy();
        server.connect(client);
        launcher.startListening();


//        var node =
//                Parser.parse(
//                        "/home/rmzs/IdeaProjects/scala-language-server/chapel-server/src/test/resources/code.java"
//                );
//        assert node != null;
//        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
//            System.out.println(node.jjtGetChild(i));
//        }
    }
}
