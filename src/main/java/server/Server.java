package server;

import java.io.*;

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

    public void run(InputStream inputStream, OutputStream outputStream) {
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
                    pending.put(new Request());
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
                        var request = parseRequest(token);
                        peek(request);
                        pending.put(request);
                    } catch (EndOfStreamException __) {
                        if (kill()) return;
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }
        }

        while (true) {
            try {
                var token = nextToken(inputStream);
                var request = parseRequest(token);
                System.out.println(request.method);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    private Request parseRequest(String token) {
        return gson.fromJson(token, Request.class);
    }

    private String nextToken(InputStream inputStream) {
        String line;
        int contentLength;
        do {
            line = readHeader(inputStream);
            contentLength = parseHeader(line);
        } while (contentLength == -1);

        readHeader(inputStream).isEmpty();

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
        var scanner = new BufferedReader(new InputStreamReader(inputStream));
        // todo Перепиши стрим в буферед ридер и передавай его всюду. Просто стрим передавать надо
        // если сам читать будешь. А мне ультра впадлу, поэтому надеюсь больше нигде стдином пользоваться не придется
        // и с буферед стримом норм пойдет

        try {
//            System.out.println(scanner.next());
            String line = scanner.readLine();
            resultBuilder.append(line);
        } catch (NoSuchElementException | IOException e) {
            throw new EndOfStreamException();
        }
        try {
            inputStream.read();
            int c = inputStream.read();
            System.out.println(c);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return resultBuilder.toString();
    }
}
