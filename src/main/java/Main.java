import server.Server;

import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        InputStream stream = new FileInputStream("src/test/resources/requestExample");
        var server = new Server();
        server.run(stream, System.out);

//        var scan = new Scanner(new File("src/test/resources/requestExample"));
//        System.out.println(scan.nextLine());
    }
}
