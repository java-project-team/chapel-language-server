/*
package ru.hse.fmcs;

import org.junit.jupiter.api.Test;
import parser.ParseException;
import parser.Parser;
import requests.BasicProcessing;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestsBasicProcessing {
    @Test
    public void rat() throws FileNotFoundException, ParseException {
//        var x = Parser.parse("/home/rmzs/IdeaProjects/scala-language-server/chapel-server/src/test/resources/code.java");
        var x = Parser.parse("/home/rmzs/IdeaProjects/scala-language-server/chapel-server/src/test/resources/a.txt");

        assert x != null;
        x.dump("");

//        var x = new Parser(new FileInputStream("/home/rmzs/IdeaProjects/scala-language-server/chapel-server/src/test/resources/code.java"));
//
//        try {
//            var n = x.File();
//            n.dump("");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Test
    public void test1() {
        BasicProcessing basicProcessing = new BasicProcessing(List.of("./src/test/resources/a.txt"));

        for (int i = 0; i < 5; i++, basicProcessing.getFileInformation("./src/test/resources/a.txt").update()) {
            var variables = basicProcessing.getFileInformation("./src/test/resources/a.txt").getVariables();
            assertEquals(3, variables.size());
            assertEquals("x", variables.get(0).getName());
            assertEquals("y", variables.get(1).getName());
            assertEquals("printLocaleName", variables.get(2).getName());

            var functions = basicProcessing.getFileInformation("./src/test/resources/a.txt").getFunctions();
            assertEquals(1, functions.size());
            assertEquals("proc", functions.get(0).getType());
            assertEquals("foo", functions.get(0).getName());
        }
    }
}
*/
