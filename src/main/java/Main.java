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
//        String port = args[0];
//        try (Socket socket = new Socket("localhost", Integer.parseInt(port))){
//            InputStream in = socket.getInputStream();
//            OutputStream out = socket.getOutputStream();

            ServerImpl server = new ServerImpl();
            Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, System.in, System.out);

            LanguageClient client = launcher.getRemoteProxy();
            server.connect(client);

            launcher.startListening();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
