package Lexer;

import java.io.*;

public class Token {
    String type;
    String val;
    int lineNumber, posBegin, posEnd;

    Token(String type, String val, int lineNumber, int posBegin, int posEnd) {
        this.lineNumber = lineNumber;
        this.posBegin = posBegin;
        this.posEnd = posEnd;
        this.val = val;
        this.type = type;
    }

    public void printFile(String path) {
        try {
            FileWriter writer = new FileWriter(path, true);
            BufferedWriter bufferWriter = new BufferedWriter(writer);
            bufferWriter.write("Type = " + type + ", val = " + val + ", line number = " + lineNumber + ", pos begin = " + posBegin + ", pos end = " + posEnd + "\n");
            bufferWriter.close();
        }
        catch (IOException e) {
            System.err.print("The res file cannot be opened\n");
        }
    }

    public void print() {
        System.out.printf("Type = %s, val = %s, line number = %d, pos begin = %d, pos end = %d\n", type, val, lineNumber, posBegin, posEnd);
    }
}