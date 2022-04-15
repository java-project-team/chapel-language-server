package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.gson.Gson;
import protocol.*;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final Gson gson = new Gson();
    private static final Logger LOG = Logger.getLogger("server");

    void run(InputStream inputStream, OutputStream outputStream) {
        var pending = new ArrayBlockingQueue<Request>(10);

        class MessageReader implements Runnable {
            void peek(Request request) {
                if (request.method.equals("$/cancelRequest")) {
                    var params = gson.fromJson(request.params, CancelParams.class);
                    var removed = pending.removeIf(r -> r.id != null && r.id.equals(params.id));
                    if (removed) LOG.info(String.format("Cancelled request %d, which had not yet started", params.id));
                    else LOG.info(String.format("Cannot cancel request %d because it has already started", params.id));
                }
            }

            private boolean kill() {
                LOG.info("Read stream has been closed, putting kill message onto queue...");
                try {
//                    pending.put(endOfStream);
                    return true;
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Failed to put kill message onto queue, will try again...", e);
                    return false;
                }
            }

            @Override
            public void run() {
                LOG.info("Placing incoming messages on queue...");

                while (true) {
                    try {
                        var token = nextToken(inputStream);
//                        var message = parseMessage(token);
//                        peek(message);
//                        pending.put(message);
                    } catch (EndOfStreamException __) {
                        if (kill()) return;
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }
        }
    }

    private String nextToken(InputStream inputStream) {
        String line;
        int contentLength;
        do {
            line = readHeader(inputStream);
            contentLength = parseHeader(line);
        } while (contentLength == -1);

        assert readHeader(inputStream).isEmpty();

        LOG.info("Header is parsed");

        return readContent(inputStream, contentLength);
    }

    private String readContent(InputStream inputStream, int contentLength) {
        try {
            int next;
            do {
                next = inputStream.read();
                if (next == -1) {
                    throw new EndOfStreamException();
                }
            } while (Character.isWhitespace((char) next));

            byte[] contentB = new byte[contentLength];
            var count = inputStream.read(contentB);
            if (count < contentLength) {
                throw new EndOfStreamException();
            }
            return new String(contentB, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new EndOfStreamException();
        }
    }

    private static int parseHeader(String header) {
        var prefix = "Content-Length: ";
        if (header.startsWith(prefix)) {
            var suffix = header.substring(prefix.length());
            return Integer.parseInt(suffix);
        }
        return -1;
    }

    static public class EndOfStreamException extends RuntimeException {
    }

    private String readHeader(InputStream inputStream) {
        var resultBuilder = new StringBuilder();
        var scanner = new Scanner(inputStream);
        try {
            String line = scanner.next(".*\r\n");
            resultBuilder.append(line);
        } catch (NoSuchElementException e) {

            throw new EndOfStreamException();
        }
        return resultBuilder.toString();
    }
}
