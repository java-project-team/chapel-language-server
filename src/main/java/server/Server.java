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
        var bufReader = new BufferedReader(new InputStreamReader(inputStream));

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
                        FileWriter f = new FileWriter("/home/rmzs/IdeaProjects/scala-language-server/src/test/requestsFromSubl");
                        var token = nextToken(bufReader);
                        var request = parseRequest(token);
                        // todo debug
//                        f.write(request.jsonrpc);
//                        f.write(request.method);
//                        f.write(request.params.toString());
                        System.out.println(request.jsonrpc);
                        System.out.println(request.method);
                        System.out.println(request.id);
                        System.out.println(request.params);

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
        var thr = new Thread(new MessageReader());
        thr.start();
//        while (true) {
//            try {
//                var token = nextToken(bufReader);
//                var request = parseRequest(token);
////                System.out.println(request);
//                System.out.println(request.jsonrpc);
//                System.out.println(request.method);
//                System.out.println(request.id);
//                System.out.println(request.params);
//            } catch (EndOfStreamException e) {
//                return;
//            } catch (Exception e) {
//                LOG.log(Level.SEVERE, e.getMessage(), e);
//            }
//        }
    }

    private Request parseRequest(String token) {
        return gson.fromJson(token, Request.class);
    }

    private String nextToken(BufferedReader bufReader) {
        String line;
        int contentLength;
        do {
            line = readHeader(bufReader);
            contentLength = parseHeader(line);
        } while (contentLength == -1);

        LOG.info("Header is parsed");

        return readContent(bufReader, contentLength);
    }

    private String readContent(BufferedReader bufReader, int contentLength) {
        try {
            int next;

            do {
                next = bufReader.read();
                if (next == -1) {
                    throw new EndOfStreamException();
                }
            } while (Character.isWhitespace((char) next));

            char[] contentB = new char[contentLength];
            contentB[0] = (char) next;
            var count = bufReader.read(contentB, 1, contentLength - 1);
            if (count < contentLength - 1) {
                throw new EndOfStreamException();
            }
            return new String(contentB);
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

    private String readHeader(BufferedReader bufReader) {
        var resultBuilder = new StringBuilder();

        try {
//            System.out.println(scanner.next());
            String line = bufReader.readLine();
//            todo закончился ли инпут
            if (line == null) {
                return "";
            }
            resultBuilder.append(line);
        } catch (NoSuchElementException | IOException e) {
            throw new RuntimeException();
        }
        return resultBuilder.toString();
    }
}
