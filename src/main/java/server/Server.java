package server;

import java.io.InputStream;
import java.io.OutputStream;
import protocol.*;

import java.util.concurrent.ArrayBlockingQueue;

public class Server {
    void run(InputStream inputStream, OutputStream outputStream) {
        var pending = new ArrayBlockingQueue<Request>(10);

    }
}
